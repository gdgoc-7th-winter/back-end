package com.project.contribution.infrastructure.outbox;

import com.project.contribution.config.ContributionOutboxProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContributionOutboxClaimService {

    private final JdbcTemplate jdbcTemplate;
    private final ContributionOutboxProperties contributionOutboxProperties;

    /**
     * PENDING 행을 선점하여 PROCESSING으로 바꾼다. 별도 트랜잭션으로 커밋되어 본문 TX와 분리된다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> claimBatch() {
        Instant now = Instant.now();
        Timestamp ts = Timestamp.from(now);
        int batchSize = contributionOutboxProperties.getBatchSize();

        if (isPostgreSql()) {
            return claimPostgresql(ts, batchSize);
        }
        return claimH2(ts, batchSize);
    }

    private boolean isPostgreSql() {
        DataSource ds = Objects.requireNonNull(jdbcTemplate.getDataSource(), "jdbcTemplate.dataSource");
        try (Connection c = ds.getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(c.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new IllegalStateException("DB 메타데이터 조회 실패", e);
        }
    }

    private List<Long> claimPostgresql(Timestamp ts, int batchSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH picked AS (");
        sql.append("SELECT id FROM contribution_outbox ");
        sql.append("WHERE status = 'PENDING' ");
        sql.append("AND (next_retry_at IS NULL OR next_retry_at <= ?) ");
        sql.append("ORDER BY created_at ASC LIMIT ? ");
        if (contributionOutboxProperties.isUseSkipLocked()) {
            sql.append("FOR UPDATE SKIP LOCKED ");
        }
        sql.append(") ");
        sql.append("UPDATE contribution_outbox o ");
        sql.append("SET status = 'PROCESSING', updated_at = ? ");
        sql.append("FROM picked ");
        sql.append("WHERE o.id = picked.id AND o.status = 'PENDING' ");
        sql.append("RETURNING o.id");

        final String sqlStr = sql.toString();
        return jdbcTemplate.query(sqlStr, (rs, rowNum) -> rs.getLong(1), ts, batchSize, ts);
    }

    private List<Long> claimH2(Timestamp ts, int batchSize) {
        String pickSql = "SELECT id FROM ("
                + "SELECT id FROM contribution_outbox "
                + "WHERE status = 'PENDING' "
                + "AND (next_retry_at IS NULL OR next_retry_at <= ?) "
                + "ORDER BY created_at ASC LIMIT ?"
                + ") AS picked";
        List<Long> candidates = jdbcTemplate.query(pickSql, (rs, rowNum) -> rs.getLong(1), ts, batchSize);
        if (candidates.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(candidates.size(), "?"));
        String updateSql = "UPDATE contribution_outbox SET status = 'PROCESSING', updated_at = ? WHERE id IN ("
                + placeholders + ") AND status = 'PENDING'";
        List<Object> updateArgs = new ArrayList<>();
        updateArgs.add(ts);
        updateArgs.addAll(candidates);
        int updated = jdbcTemplate.update(updateSql, updateArgs.toArray());
        if (updated == 0) {
            return List.of();
        }
        String confirmSql = "SELECT id FROM contribution_outbox WHERE status = 'PROCESSING' AND id IN ("
                + placeholders + ")";
        return jdbcTemplate.query(confirmSql, (rs, rowNum) -> rs.getLong(1), candidates.toArray());
    }
}
