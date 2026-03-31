package com.project.contribution.application.event;

import com.project.contribution.application.service.ContributionFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContributionActivityAfterCommitListener {

    private final ContributionFacade contributionFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContributionActivity(ContributionActivityEvent event) {
        try {
            contributionFacade.applyActivity(event.context());
        } catch (Exception e) {
            log.error(
                    "기여 점수 반영 실패(커밋 후). activity={}, subjectUserId={}, referenceId={}",
                    event.context().activityType(),
                    event.context().subjectUserId(),
                    event.context().referenceId(),
                    e);
        }
    }
}
