package com.project.ranking.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.ranking.application.dto.RankingEntryResponse;
import com.project.ranking.application.dto.RankingListResponse;
import com.project.ranking.application.dto.RankingMeResponse;
import com.project.ranking.application.dto.RankingMetaResponse;
import com.project.ranking.application.dto.RankingPageInfo;
import com.project.ranking.application.support.RankingPeriodKeys;
import com.project.ranking.config.RankingProperties;
import com.project.ranking.domain.RankingPeriodType;
import com.project.ranking.domain.repository.RankingSnapshotRepository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 랭킹 목록·내 순위 조회.
 * <p>
 * {@code displayRank}: 탈퇴자를 제외한 뒤 {@code score}만 기준으로 한 표준 SQL {@code RANK()}
 * (동점은 동일 순위, 다음 순위는 간격 있음 — 예: 1,2,2,4).
 * 동점 구간 내 표시 순서는 목록 쿼리에서 {@code user_id ASC}로 고정한다.
 * 운영(PostgreSQL)은 {@link RankingProperties#isUseWindowRankSql()} 가 true일 때 윈도우 함수,
 * 테스트(H2) 등에서는 동일 의미의 대체식을 쓴다. 목록과 COUNT는 동일 {@code filtered} CTE를 쓴다.
 */
@Service
public class RankingQueryService {

    private final NamedParameterJdbcTemplate jdbc;
    private final RankingSnapshotRepository snapshotRepository;
    private final RankingMetaFactory metaFactory;
    private final RankingProperties rankingProperties;
    private final String listSql;
    private final String meSql;

    public RankingQueryService(
            NamedParameterJdbcTemplate jdbc,
            RankingSnapshotRepository snapshotRepository,
            RankingMetaFactory metaFactory,
            RankingProperties rankingProperties) {
        this.jdbc = jdbc;
        this.snapshotRepository = snapshotRepository;
        this.metaFactory = metaFactory;
        this.rankingProperties = rankingProperties;
        String filteredCte = buildFilteredCte(rankingProperties);
        this.listSql = filteredCte + LIST_SQL_SUFFIX;
        this.meSql = filteredCte + ME_SQL_SUFFIX;
    }

    @Transactional(readOnly = true)
    public RankingListResponse list(
            RankingPeriodType periodType,
            String periodKeyOrNull,
            int page,
            int size,
            Long currentUserIdOrNull) {
        int cappedSize = Math.min(Math.max(1, size), rankingProperties.getMaxPageSize());
        int zeroBasedPage = Math.max(0, page);
        String periodKey = resolvePeriodKey(periodType, periodKeyOrNull);
        Optional<Instant> calculatedAtOpt = snapshotRepository.findMaxCalculatedAt(periodType, periodKey);
        Instant calculatedAt = calculatedAtOpt.orElse(null);
        RankingMetaResponse meta = metaFactory.build(periodType, periodKey, calculatedAt);

        long total = countFiltered(periodType, periodKey);
        int totalPages = cappedSize == 0 ? 0 : (int) Math.ceil((double) total / (double) cappedSize);
        int offset = zeroBasedPage * cappedSize;

        MapSqlParameterSource p = baseParams(periodType, periodKey)
                .addValue("limit", cappedSize)
                .addValue("offset", offset);

        List<RankingEntryResponse> rows = jdbc.query(listSql, p, listRowMapper(currentUserIdOrNull));

        RankingPageInfo pageInfo = new RankingPageInfo(zeroBasedPage, cappedSize, total, totalPages);
        return new RankingListResponse(meta, pageInfo, rows);
    }

    @Transactional(readOnly = true)
    public RankingMeResponse myRank(
            RankingPeriodType periodType,
            String periodKeyOrNull,
            long userId) {
        String periodKey = resolvePeriodKey(periodType, periodKeyOrNull);
        Optional<Instant> calculatedAtOpt = snapshotRepository.findMaxCalculatedAt(periodType, periodKey);
        Instant calculatedAt = calculatedAtOpt.orElseThrow(
                () -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "랭킹 스냅샷이 없습니다."));
        RankingMetaResponse meta = metaFactory.build(periodType, periodKey, calculatedAt);

        MapSqlParameterSource p = baseParams(periodType, periodKey).addValue("meUserId", userId);
        List<RankingMeResponse> rows = jdbc.query(meSql, p, (rs, rowNum) -> mapMe(rs, meta));
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "해당 기간 랭킹에 포함되지 않았습니다.");
        }
        return rows.getFirst();
    }

    private long countFiltered(RankingPeriodType periodType, String periodKey) {
        Long c = jdbc.queryForObject(COUNT_SQL, baseParams(periodType, periodKey), Long.class);
        return c != null ? c : 0L;
    }

    private static MapSqlParameterSource baseParams(RankingPeriodType periodType, String periodKey) {
        return new MapSqlParameterSource()
                .addValue("pt", periodType.name())
                .addValue("pk", periodKey);
    }

    private String resolvePeriodKey(RankingPeriodType periodType, String periodKeyOrNull) {
        if (periodKeyOrNull != null && !periodKeyOrNull.isBlank()) {
            return validateProvidedKey(periodType, periodKeyOrNull.trim());
        }
        LocalDate today = RankingPeriodKeys.todaySeoul();
        return switch (periodType) {
            case ALL_TIME -> RankingPeriodKeys.ALL_TIME_KEY;
            case WEEKLY -> RankingPeriodKeys.defaultWeeklyPeriodKey(today);
            case MONTHLY -> RankingPeriodKeys.defaultMonthlyPeriodKey(today);
        };
    }

    private static String validateProvidedKey(RankingPeriodType periodType, String key) {
        return switch (periodType) {
            case ALL_TIME -> {
                if (!RankingPeriodKeys.ALL_TIME_KEY.equals(key)) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "ALL_TIME period_key는 ALL 이어야 합니다.");
                }
                yield key;
            }
            case WEEKLY -> {
                RankingPeriodKeys.parseWeekMondayOrThrow(key);
                yield key;
            }
            case MONTHLY -> {
                RankingPeriodKeys.parseYearMonthOrThrow(key);
                yield key;
            }
        };
    }

    private static RowMapper<RankingEntryResponse> listRowMapper(Long currentUserIdOrNull) {
        return (rs, rowNum) -> new RankingEntryResponse(
                rs.getInt("display_rank"),
                rs.getObject("snapshot_rank") != null ? rs.getInt("snapshot_rank") : null,
                rs.getLong("score"),
                rs.getLong("user_id"),
                rs.getString("nickname"),
                rs.getString("department_name"),
                rs.getString("level_badge_image"),
                currentUserIdOrNull != null && currentUserIdOrNull == rs.getLong("user_id"));
    }

    private static RankingMeResponse mapMe(ResultSet rs, RankingMetaResponse meta) throws SQLException {
        return new RankingMeResponse(
                meta.periodType(),
                meta.periodKey(),
                meta.calculatedAt(),
                meta.periodStart(),
                meta.periodEnd(),
                meta.isRealtime(),
                rs.getInt("display_rank"),
                rs.getObject("snapshot_rank") != null ? rs.getInt("snapshot_rank") : null,
                rs.getLong("score"),
                rs.getLong("user_id"),
                rs.getString("nickname"),
                rs.getString("department_name"),
                rs.getString("level_badge_image"));
    }

    private static String buildFilteredCte(RankingProperties rankingProperties) {
        String rankedInner = rankingProperties.isUseWindowRankSql()
                ? """
                SELECT f.*,
                       RANK() OVER (ORDER BY f.score DESC) AS display_rank
                FROM filtered f
                """
                : """
                SELECT f.*,
                       (1 + (SELECT COUNT(*) FROM filtered f2 WHERE f2.score > f.score)) AS display_rank
                FROM filtered f
                """;
        return """
                WITH filtered AS (
                    SELECT rs.user_id, rs.score, rs.snapshot_rank, rs.calculated_at
                    FROM ranking_snapshot rs
                    INNER JOIN users u ON u.user_id = rs.user_id AND u.deleted_at IS NULL
                    WHERE rs.period_type = :pt AND rs.period_key = :pk
                ),
                ranked AS (
                """
                + rankedInner
                + """
                )
                """;
    }

    private static final String COUNT_SQL = """
            WITH filtered AS (
                SELECT rs.user_id
                FROM ranking_snapshot rs
                INNER JOIN users u ON u.user_id = rs.user_id AND u.deleted_at IS NULL
                WHERE rs.period_type = :pt AND rs.period_key = :pk
            )
            SELECT COUNT(*) FROM filtered
            """;

    private static final String LIST_SQL_SUFFIX = """
            SELECT r.display_rank, r.score, r.snapshot_rank, r.user_id, u.nickname,
                   d.name AS department_name, lb.level_image AS level_badge_image
            FROM ranked r
            INNER JOIN users u ON u.user_id = r.user_id
            LEFT JOIN departments d ON d.department_id = u.department_id
            LEFT JOIN level_badge lb ON lb.id = u.level_id
            ORDER BY r.display_rank ASC, r.user_id ASC
            LIMIT :limit OFFSET :offset
            """;

    private static final String ME_SQL_SUFFIX = """
            SELECT r.display_rank, r.score, r.snapshot_rank, r.user_id, u.nickname,
                   d.name AS department_name, lb.level_image AS level_badge_image
            FROM ranked r
            INNER JOIN users u ON u.user_id = r.user_id
            LEFT JOIN departments d ON d.department_id = u.department_id
            LEFT JOIN level_badge lb ON lb.id = u.level_id
            WHERE r.user_id = :meUserId
            """;
}
