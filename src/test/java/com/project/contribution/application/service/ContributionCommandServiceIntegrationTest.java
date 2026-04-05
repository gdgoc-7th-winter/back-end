package com.project.contribution.application.service;

import com.project.contribution.domain.repository.ContributionScoreRepository;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.project.contribution.domain.support.ContributionScoreCodes;
import com.project.global.event.ActivityType;
import com.project.user.application.dto.EarnScoreResult;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ContributionCommandServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private ContributionCommandService contributionCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LevelBadgeRepository levelBadgeRepository;

    @Autowired
    private ContributionScoreRepository contributionScoreRepository;

    @Autowired
    private UserContributionRepository userContributionRepository;

    private Long userId;
    /** 시드 levelBadges.json — 0~9 (Dummy) */
    private LevelBadge dummyTier;
    /** 시드 levelBadges.json — 600~1199 (Gold), 상한 초과 시 최상위 티어 검증에 사용 */
    private LevelBadge goldTier;

    @BeforeEach
    void setUp() {
        userContributionRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush();

        dummyTier = levelBadgeRepository.findByPointWithinRange(0).orElseThrow();
        goldTier = levelBadgeRepository.findByPointWithinRange(700).orElseThrow();

        contributionScoreRepository.findByCode(ContributionScoreCodes.POST_WRITE).orElseThrow();

        User user = User.builder()
                .email("contrib-int-" + UUID.randomUUID() + "@test.com")
                .password("pw")
                .nickname("tester")
                .build();
        user.initializeLevelBadge(dummyTier);
        userId = userRepository.save(user).getId();
    }

    @Test
    @DisplayName("grantScore는 원장·total_point·level_id를 한 트랜잭션에서 반영한다")
    @Transactional
    void grantScoreSuccessUpdatesPointAndLevel() {
        EarnScoreResult first = contributionCommandService.grantScore(
                userId, ContributionScoreCodes.POST_WRITE, 1L, ActivityType.POST_CREATED, null);

        assertThat(first.grantedNewLedger()).isTrue();
        User u1 = first.user();
        assertThat(u1.getTotalPoint()).isEqualTo(5);
        assertThat(u1.getLevelBadge().getId()).isEqualTo(dummyTier.getId());
    }

    @Test
    @DisplayName("동일 idempotency(동일 user·score·reference)로 grantScore를 두 번 호출하면 두 번째는 스킵된다")
    @Transactional
    void grantScoreIdempotentSecondCallSkipsLedger() {
        contributionCommandService.grantScore(
                userId, ContributionScoreCodes.POST_WRITE, 42L, ActivityType.POST_CREATED, null);
        EarnScoreResult second = contributionCommandService.grantScore(
                userId, ContributionScoreCodes.POST_WRITE, 42L, ActivityType.POST_CREATED, null);

        assertThat(second.grantedNewLedger()).isFalse();
        assertThat(second.user().getTotalPoint()).isEqualTo(5);
        assertThat(userContributionRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("revokeScore는 GRANT 원장이 있을 때만 점수를 깎고 멱등 키로 중복 회수를 막는다")
    @Transactional
    void revokeScoreSuccessAndIdempotent() {
        contributionCommandService.grantScore(
                userId, ContributionScoreCodes.POST_WRITE, 99L, ActivityType.POST_CREATED, null);

        EarnScoreResult revoke1 = contributionCommandService.revokeScore(
                userId, ContributionScoreCodes.POST_WRITE, 99L, ActivityType.POST_DELETED, "del-1", null);
        assertThat(revoke1.grantedNewLedger()).isTrue();
        assertThat(revoke1.user().getTotalPoint()).isEqualTo(0);

        EarnScoreResult revoke2 = contributionCommandService.revokeScore(
                userId, ContributionScoreCodes.POST_WRITE, 99L, ActivityType.POST_DELETED, "del-1", null);
        assertThat(revoke2.grantedNewLedger()).isFalse();
        assertThat(revoke2.user().getTotalPoint()).isEqualTo(0);
    }

    @Test
    @DisplayName("총점이 상위 구간이면 level_id가 해당 티어로 맞춰진다 (시드 구간 기준)")
    @Transactional
    void grantScoreManyGrantsMapsExpectedTier() {
        for (int i = 0; i < 200; i++) {
            contributionCommandService.grantScore(
                    userId, ContributionScoreCodes.POST_WRITE, 10_000L + i, ActivityType.POST_CREATED, null);
        }
        User u = userRepository.findActiveByIdLean(userId).orElseThrow();
        assertThat(u.getTotalPoint()).isEqualTo(200 * 5);
        assertThat(u.getLevelBadge().getId()).isEqualTo(goldTier.getId());
    }

    /**
     * 병렬 호출 시 각 요청이 별도 트랜잭션으로 커밋되므로 {@code total_point += delta} 원자 UPDATE로 유실이 나지 않아야 한다.
     * (동시성 극한 검증은 부하/DB별 통합 환경에서 추가하는 것이 일반적이다.)
     */
    @Test
    @DisplayName("서로 다른 reference로 병렬 grant 시 합계가 유실되지 않는다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentGrantsWithDistinctReferencesSumMatches() throws Exception {
        int n = 10;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        AtomicInteger failures = new AtomicInteger();

        for (int i = 0; i < n; i++) {
            long ref = 5000L + i;
            pool.submit(() -> {
                try {
                    start.await();
                    contributionCommandService.grantScore(
                            userId, ContributionScoreCodes.POST_WRITE, ref, ActivityType.POST_CREATED, null);
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();
        assertThat(failures.get()).isZero();

        User u = userRepository.findActiveByIdLean(userId).orElseThrow();
        assertThat(u.getTotalPoint()).isEqualTo(5 * n);
    }
}
