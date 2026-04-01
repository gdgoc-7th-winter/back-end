package com.project.contribution.infrastructure.outbox;

import com.project.contribution.config.ContributionOutboxProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id FROM contribution_outbox WHERE status = 'PENDING' ");
        sql.append("AND (next_retry_at IS NULL OR next_retry_at <= ?) ");
        sql.append("ORDER BY created_at ASC LIMIT ?");
        if (contributionOutboxProperties.isUseSkipLocked()) {
            sql.append(" FOR UPDATE SKIP LOCKED");
        }
        List<Long> ids = jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> rs.getLong(1),
                ts,
                contributionOutboxProperties.getBatchSize());
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String updateSql = "UPDATE contribution_outbox SET status = 'PROCESSING', updated_at = ? WHERE id IN ("
                + placeholders + ")";
        List<Object> args = new ArrayList<>();
        args.add(ts);
        args.addAll(ids);
        jdbcTemplate.update(updateSql, args.toArray());
        return ids;
    }
}
