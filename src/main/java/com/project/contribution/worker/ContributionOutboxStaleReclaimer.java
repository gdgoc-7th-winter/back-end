package com.project.contribution.worker;

import com.project.contribution.config.ContributionOutboxProperties;
import com.project.contribution.domain.ContributionOutboxStatus;
import com.project.contribution.infrastructure.persistence.ContributionOutboxJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContributionOutboxStaleReclaimer {

    private final ContributionOutboxJpaRepository contributionOutboxJpaRepository;
    private final ContributionOutboxProperties contributionOutboxProperties;

    @Scheduled(fixedDelayString = "${contribution.outbox.reclaim-interval-ms:60000}")
    @Transactional
    public void reclaimStaleProcessing() {
        Instant before = Instant.now().minus(contributionOutboxProperties.getStaleProcessingMinutes(), ChronoUnit.MINUTES);
        int n = contributionOutboxJpaRepository.reclaimStaleProcessing(
                ContributionOutboxStatus.PENDING,
                ContributionOutboxStatus.PROCESSING,
                before,
                "stale processing reclaimed",
                Instant.now());
        if (n > 0) {
            log.info("Outbox stale PROCESSING {}건을 PENDING으로 되돌렸습니다.", n);
        }
    }
}
