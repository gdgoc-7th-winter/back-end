package com.project.ranking.application.service;

import com.project.contribution.worker.ContributionOutboxStaleReclaimer;
import com.project.contribution.worker.ContributionOutboxWorker;
import com.project.ranking.application.support.RankingPeriodKeys;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RankingSnapshotBatchServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private ContributionOutboxWorker contributionOutboxWorker;

    @MockitoBean
    private ContributionOutboxStaleReclaimer contributionOutboxStaleReclaimer;

    @Autowired
    private RankingSnapshotRebuildService snapshotRebuildService;

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
    @DisplayName("ALL_TIME 배치는 멱등이며 RANK 동점 처리(1,1,3)를 저장한다")
    void allTimeBatchIsIdempotentAndUsesRank() {
        LevelBadge badge = levelBadgeRepository.findByPointWithinRange(0).orElseThrow();
        User u1 = persistUser(badge, 100);
        User u2 = persistUser(badge, 100);
        User u3 = persistUser(badge, 50);

        Instant t = Instant.parse("2026-04-01T00:00:00Z");
        snapshotRebuildService.rebuildAllTime(t);
        snapshotRebuildService.rebuildAllTime(t);

        List<Integer> ranks = jdbcTemplate.query(
                """
                SELECT snapshot_rank FROM ranking_snapshot
                WHERE period_type = 'ALL_TIME' AND period_key = 'ALL'
                ORDER BY user_id
                """,
                (rs, i) -> rs.getInt(1));
        assertThat(ranks).containsExactly(1, 1, 3);
    }

    @Test
    @DisplayName("동일 ALL_TIME period 재실행 후에도 행 수는 동일(멱등)")
    void uniqueReplacePromotion() {
        LevelBadge badge = levelBadgeRepository.findByPointWithinRange(0).orElseThrow();
        persistUser(badge, 10);

        Instant t = Instant.now();
        snapshotRebuildService.rebuildAllTime(t);
        long c = countAllTimeRows();
        assertThat(c).isEqualTo(1L);

        snapshotRebuildService.rebuildAllTime(t);
        assertThat(countAllTimeRows()).isEqualTo(c);
    }

    @Test
    @DisplayName("WEEKLY 스냅샷은 occurred_at 구간 합산만 반영한다")
    void weeklySnapshotUsesOccurredAtWindow() {
        LevelBadge badge = levelBadgeRepository.findByPointWithinRange(0).orElseThrow();
        User u = persistUser(badge, 0);

        LocalDate monday = LocalDate.of(2026, 3, 30);
        String weekKey = RankingPeriodKeys.formatWeekKey(monday);
        Instant start = RankingPeriodKeys.weekStartInclusive(monday);
        Instant end = RankingPeriodKeys.weekEndExclusive(monday);

        Long contributionScoreId = jdbcTemplate.queryForObject(
                "SELECT cont_id FROM contribution_score WHERE cont_code = 'POST_WRITE' LIMIT 1",
                Long.class);

        jdbcTemplate.update(
                """
                INSERT INTO user_contribution
                (user_id, contribution_id, reference_id, entry_type, signed_point, occurred_at,
                 idempotency_key, activity_type, reference_type, created_at, updated_at)
                VALUES (?, ?, 1, 'GRANT', 5, ?, ?, 'POST_CREATED', 'POST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                u.getId(),
                contributionScoreId,
                java.sql.Timestamp.from(start.plusSeconds(3600)),
                "wk-" + UUID.randomUUID());

        jdbcTemplate.update(
                """
                INSERT INTO user_contribution
                (user_id, contribution_id, reference_id, entry_type, signed_point, occurred_at,
                 idempotency_key, activity_type, reference_type, created_at, updated_at)
                VALUES (?, ?, 2, 'GRANT', 99, ?, ?, 'POST_CREATED', 'POST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                u.getId(),
                contributionScoreId,
                java.sql.Timestamp.from(end.plusSeconds(1)),
                "wk2-" + UUID.randomUUID());

        snapshotRebuildService.rebuildWeekly(weekKey, start, end, Instant.now());

        Long score = jdbcTemplate.queryForObject(
                "SELECT score FROM ranking_snapshot WHERE period_type = 'WEEKLY' AND period_key = ? AND user_id = ?",
                Long.class,
                weekKey,
                u.getId());
        assertThat(score).isEqualTo(5L);
    }

    private long countAllTimeRows() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ranking_snapshot WHERE period_type = 'ALL_TIME' AND period_key = 'ALL'",
                Long.class);
    }

    private User persistUser(LevelBadge badge, int totalPoint) {
        User u = User.builder()
                .email("r-" + UUID.randomUUID() + "@test.com")
                .password("pw")
                .nickname("n-" + UUID.randomUUID().toString().substring(0, 24))
                .build();
        u.initializeLevelBadge(badge);
        User saved = userRepository.save(u);
        jdbcTemplate.update("UPDATE users SET total_point = ? WHERE user_id = ?", totalPoint, saved.getId());
        return userRepository.findById(saved.getId()).orElseThrow();
    }
}
