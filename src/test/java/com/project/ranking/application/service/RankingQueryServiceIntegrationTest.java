package com.project.ranking.application.service;

import com.project.contribution.worker.ContributionOutboxStaleReclaimer;
import com.project.contribution.worker.ContributionOutboxWorker;
import com.project.ranking.application.dto.RankingListResponse;
import com.project.ranking.domain.RankingPeriodType;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RankingQueryServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private ContributionOutboxWorker contributionOutboxWorker;

    @MockitoBean
    private ContributionOutboxStaleReclaimer contributionOutboxStaleReclaimer;

    @Autowired
    private RankingSnapshotBatchService batchService;

    @Autowired
    private RankingQueryService rankingQueryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LevelBadgeRepository levelBadgeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.update("DELETE FROM ranking_snapshot");
        jdbcTemplate.update("DELETE FROM ranking_snapshot_staging");
        jdbcTemplate.update("DELETE FROM user_contribution");
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("탈퇴 유저는 스냅샷에 남아 있어도 목록·COUNT에서 제외되고 displayRank가 재계산된다")
    void deletedUsersExcludedFromDisplayRankAndCount() {
        LevelBadge badge = levelBadgeRepository.findByPointWithinRange(0).orElseThrow();
        User active = persistUser(badge, 10);
        User withdrawn = persistUser(badge, 20);

        batchService.rebuildAllTime(Instant.now());

        RankingListResponse before = rankingQueryService.list(RankingPeriodType.ALL_TIME, "ALL", 0, 10, null);
        assertThat(before.page().totalElements()).isEqualTo(2L);
        assertThat(before.content().getFirst().displayRank()).isEqualTo(1);

        withdrawn.withdraw();
        userRepository.saveAndFlush(withdrawn);

        RankingListResponse after = rankingQueryService.list(RankingPeriodType.ALL_TIME, "ALL", 0, 10, null);
        assertThat(after.page().totalElements()).isEqualTo(1L);
        assertThat(after.content()).hasSize(1);
        assertThat(after.content().getFirst().displayRank()).isEqualTo(1);
        assertThat(after.content().getFirst().userId()).isEqualTo(active.getId());
    }

    @Test
    @DisplayName("목록은 DB RANK 결과와 동일한 displayRank를 반환한다")
    void displayRankMatchesRankInDatabase() {
        LevelBadge badge = levelBadgeRepository.findByPointWithinRange(0).orElseThrow();
        persistUser(badge, 100);
        persistUser(badge, 100);

        batchService.rebuildAllTime(Instant.now());

        RankingListResponse res = rankingQueryService.list(RankingPeriodType.ALL_TIME, "ALL", 0, 10, null);
        assertThat(res.content().get(0).displayRank()).isEqualTo(1);
        assertThat(res.content().get(1).displayRank()).isEqualTo(1);
    }

    private User persistUser(LevelBadge badge, int totalPoint) {
        User u = User.builder()
                .email("q-" + UUID.randomUUID() + "@test.com")
                .password("pw")
                .nickname("n-" + UUID.randomUUID().toString().substring(0, 24))
                .build();
        u.initializeLevelBadge(badge);
        User saved = userRepository.save(u);
        jdbcTemplate.update("UPDATE users SET total_point = ? WHERE user_id = ?", totalPoint, saved.getId());
        return userRepository.findById(saved.getId()).orElseThrow();
    }
}
