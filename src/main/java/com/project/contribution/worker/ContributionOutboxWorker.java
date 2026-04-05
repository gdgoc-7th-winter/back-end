package com.project.contribution.worker;

import com.project.contribution.infrastructure.outbox.ContributionOutboxClaimService;
import com.project.contribution.infrastructure.outbox.ContributionOutboxFailureHandler;
import com.project.contribution.infrastructure.outbox.ContributionOutboxProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContributionOutboxWorker {

    private final ContributionOutboxClaimService contributionOutboxClaimService;
    private final ContributionOutboxProcessor contributionOutboxProcessor;
    private final ContributionOutboxFailureHandler contributionOutboxFailureHandler;

    @Scheduled(fixedDelayString = "${contribution.outbox.poll-interval-ms:500}")
    public void poll() {
        try {
            List<Long> ids = contributionOutboxClaimService.claimBatch();
            for (Long id : ids) {
                try {
                    contributionOutboxProcessor.processAndMarkDone(id);
                } catch (Exception e) {
                    contributionOutboxFailureHandler.recordFailure(id, e);
                }
            }
        } catch (Exception e) {
            log.error("Outbox claim/poll 실패", e);
        }
    }
}
