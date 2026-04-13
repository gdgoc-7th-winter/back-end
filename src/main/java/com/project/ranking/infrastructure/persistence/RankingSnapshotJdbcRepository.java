package com.project.ranking.infrastructure.persistence;

import com.project.ranking.config.RankingProperties;
import com.project.ranking.domain.RankingPeriodType;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * 배치용 네이티브 SQL — 집계 후 순위 부여·스테이징→본 테이블 프로모션.
 * <p>
 * 트랜잭션 경계는 호출하는 {@link com.project.ranking.application.service.RankingSnapshotRebuildService}에서 잡는다.
 * <p>
 * 운영(PostgreSQL)은 {@code RANK() OVER (ORDER BY score DESC)}(설정 기본).
 * H2 통합 테스트는 {@link RankingProperties#isUseWindowRankSql()} 가 false일 때 동점에 표준 {@code RANK}와 동일한 식을 쓴다.
 */
@Repository
public class RankingSnapshotJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final String insertAllTimeStagingSql;
    private final String insertWeeklyStagingSql;
    private final String insertMonthlyStagingSql;

    public RankingSnapshotJdbcRepository(
            NamedParameterJdbcTemplate jdbc,
            RankingProperties rankingProperties) {
        this.jdbc = jdbc;
        this.insertAllTimeStagingSql = buildInsertAllTimeStagingSql(rankingProperties);
        this.insertWeeklyStagingSql = buildInsertWeeklyStagingSql(rankingProperties);
        this.insertMonthlyStagingSql = buildInsertMonthlyStagingSql(rankingProperties);
    }

    public void deleteStaging(RankingPeriodType periodType, String periodKey) {
        jdbc.update(
                """
                DELETE FROM ranking_snapshot_staging
                WHERE period_type = :periodType AND period_key = :periodKey
                """,
                new MapSqlParameterSource()
                        .addValue("periodType", periodType.name())
                        .addValue("periodKey", periodKey));
    }

    public void insertAllTimeStaging(Instant calculatedAt) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("calculatedAt", Timestamp.from(calculatedAt));
        jdbc.update(insertAllTimeStagingSql, p);
    }

    public void insertWeeklyStaging(
            Instant occurredAtStartInclusive,
            Instant occurredAtEndExclusive,
            String periodKey,
            Instant calculatedAt) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("start", Timestamp.from(occurredAtStartInclusive))
                .addValue("endEx", Timestamp.from(occurredAtEndExclusive))
                .addValue("periodKey", periodKey)
                .addValue("calculatedAt", Timestamp.from(calculatedAt));
        jdbc.update(insertWeeklyStagingSql, p);
    }

    public void insertMonthlyStaging(
            Instant occurredAtStartInclusive,
            Instant occurredAtEndExclusive,
            String periodKey,
            Instant calculatedAt) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("start", Timestamp.from(occurredAtStartInclusive))
                .addValue("endEx", Timestamp.from(occurredAtEndExclusive))
                .addValue("periodKey", periodKey)
                .addValue("calculatedAt", Timestamp.from(calculatedAt));
        jdbc.update(insertMonthlyStagingSql, p);
    }

    /**
     * 본 테이블에서 해당 기간 행을 삭제한 뒤 스테이징에서 복사한다.
     * 호출부({@code RankingSnapshotRebuildService}의 {@code @Transactional})와 같은 트랜잭션에서 실행되어야 한다.
     */
    public void promoteStagingToMain(RankingPeriodType periodType, String periodKey) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("periodType", periodType.name())
                .addValue("periodKey", periodKey);
        jdbc.update(
                """
                DELETE FROM ranking_snapshot
                WHERE period_type = :periodType AND period_key = :periodKey
                """,
                p);
        jdbc.update(
                """
                INSERT INTO ranking_snapshot
                    (period_type, period_key, user_id, score, snapshot_rank, calculated_at)
                SELECT period_type, period_key, user_id, score, snapshot_rank, calculated_at
                FROM ranking_snapshot_staging
                WHERE period_type = :periodType AND period_key = :periodKey
                """,
                p);
    }

    public void deleteStagingAfterPromote(RankingPeriodType periodType, String periodKey) {
        deleteStaging(periodType, periodKey);
    }

    private static String buildInsertAllTimeStagingSql(RankingProperties p) {
        String rankExpr = p.isUseWindowRankSql()
                ? "RANK() OVER (ORDER BY u.total_point DESC)"
                : "1 + (SELECT COUNT(*) FROM users u2 WHERE u2.deleted_at IS NULL AND u2.total_point > u.total_point)";
        return """
                INSERT INTO ranking_snapshot_staging
                    (period_type, period_key, user_id, score, snapshot_rank, calculated_at)
                SELECT
                    'ALL_TIME', 'ALL', u.user_id, u.total_point,
                    """
                + rankExpr
                + """
                ,
                    :calculatedAt
                FROM users u
                WHERE u.deleted_at IS NULL
                """;
    }

    private static String buildInsertWeeklyStagingSql(RankingProperties p) {
        String rankExpr = p.isUseWindowRankSql()
                ? "RANK() OVER (ORDER BY agg.period_score DESC)"
                : """
                1 + (SELECT COUNT(*) FROM (
                        SELECT uc2.user_id AS uid, SUM(uc2.signed_point) AS ps
                        FROM user_contribution uc2
                        INNER JOIN users u2 ON u2.user_id = uc2.user_id AND u2.deleted_at IS NULL
                        WHERE uc2.occurred_at >= :start AND uc2.occurred_at < :endEx
                        GROUP BY uc2.user_id
                    ) x WHERE x.ps > agg.period_score)""";
        return """
                INSERT INTO ranking_snapshot_staging
                    (period_type, period_key, user_id, score, snapshot_rank, calculated_at)
                SELECT
                    'WEEKLY', :periodKey, agg.user_id, agg.period_score,
                    """
                + rankExpr
                + """
                ,
                    :calculatedAt
                FROM (
                    SELECT uc.user_id AS user_id, SUM(uc.signed_point) AS period_score
                    FROM user_contribution uc
                    INNER JOIN users u ON u.user_id = uc.user_id AND u.deleted_at IS NULL
                    WHERE uc.occurred_at >= :start AND uc.occurred_at < :endEx
                    GROUP BY uc.user_id
                ) agg
                """;
    }

    private static String buildInsertMonthlyStagingSql(RankingProperties p) {
        String rankExpr = p.isUseWindowRankSql()
                ? "RANK() OVER (ORDER BY agg.period_score DESC)"
                : """
                1 + (SELECT COUNT(*) FROM (
                        SELECT uc2.user_id AS uid, SUM(uc2.signed_point) AS ps
                        FROM user_contribution uc2
                        INNER JOIN users u2 ON u2.user_id = uc2.user_id AND u2.deleted_at IS NULL
                        WHERE uc2.occurred_at >= :start AND uc2.occurred_at < :endEx
                        GROUP BY uc2.user_id
                    ) x WHERE x.ps > agg.period_score)""";
        return """
                INSERT INTO ranking_snapshot_staging
                    (period_type, period_key, user_id, score, snapshot_rank, calculated_at)
                SELECT
                    'MONTHLY', :periodKey, agg.user_id, agg.period_score,
                    """
                + rankExpr
                + """
                ,
                    :calculatedAt
                FROM (
                    SELECT uc.user_id AS user_id, SUM(uc.signed_point) AS period_score
                    FROM user_contribution uc
                    INNER JOIN users u ON u.user_id = uc.user_id AND u.deleted_at IS NULL
                    WHERE uc.occurred_at >= :start AND uc.occurred_at < :endEx
                    GROUP BY uc.user_id
                ) agg
                """;
    }
}
