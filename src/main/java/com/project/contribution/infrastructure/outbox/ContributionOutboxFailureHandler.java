package com.project.contribution.infrastructure.outbox;

import com.project.contribution.config.ContributionOutboxProperties;
import com.project.contribution.domain.ContributionOutboxStatus;
import com.project.contribution.infrastructure.persistence.ContributionOutboxJpaRepository;
import com.project.contribution.infrastructure.persistence.entity.ContributionOutboxEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContributionOutboxFailureHandler {

    private final ContributionOutboxJpaRepository contributionOutboxJpaRepository;
    private final ContributionOutboxProperties contributionOutboxProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long id, Throwable error) {
        ContributionOutboxEntity row = contributionOutboxJpaRepository.findById(id).orElse(null);
        if (row == null || row.getStatus() != ContributionOutboxStatus.PROCESSING) {
            return;
        }
        String msg = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
        row.recoverAfterProcessingFailure(msg, contributionOutboxProperties.getMaxAttempts(), Instant.now());
        contributionOutboxJpaRepository.save(row);
        log.warn("Outbox 처리 실패 id={}, attempts={}, status={}, err={}", id, row.getAttempts(), row.getStatus(), msg);
    }
}
