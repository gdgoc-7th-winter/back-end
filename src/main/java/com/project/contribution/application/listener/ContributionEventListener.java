package com.project.contribution.application.listener;

import com.project.contribution.application.service.ContributionService;
import com.project.global.event.ActivityEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ContributionEventListener {
    private final ContributionService contributionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPromotionForBadge(ActivityEvent activityEvent) {
        contributionService.checkAndGrantBadges(activityEvent.userId(), activityEvent.activityType());
    }
}
