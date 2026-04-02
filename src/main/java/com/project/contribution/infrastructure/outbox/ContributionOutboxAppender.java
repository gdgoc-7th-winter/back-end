package com.project.contribution.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.port.ContributionOutboxPort;
import com.project.contribution.application.support.ActivityContextDedupKeys;
import com.project.contribution.infrastructure.persistence.ContributionOutboxJpaRepository;
import com.project.contribution.infrastructure.persistence.entity.ContributionOutboxEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContributionOutboxAppender implements ContributionOutboxPort {

    private final ObjectMapper objectMapper;
    private final ContributionOutboxJpaRepository contributionOutboxJpaRepository;

    @Override
    public void append(ActivityContext context) {
        String dedupKey = ActivityContextDedupKeys.of(context);
        String payload;
        try {
            payload = objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("ActivityContext JSON 직렬화 실패", e);
        }
        ContributionOutboxEntity row = ContributionOutboxEntity.pending(
                context.activityType(), payload, dedupKey, Instant.now());
        try {
            contributionOutboxJpaRepository.save(row);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateDedupKey(e)) {
                log.debug("Outbox dedup_key 중복 무시: {}", dedupKey);
                return;
            }
            throw e;
        }
    }

    private static boolean isDuplicateDedupKey(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof SQLException sqlEx) {
            if ("23505".equals(sqlEx.getSQLState())) {
                return true;
            }
        }
        String msg = cause.getMessage();
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase(Locale.ROOT);
        return lower.contains("dedup") || lower.contains("unique") || lower.contains("uk_");
    }
}
