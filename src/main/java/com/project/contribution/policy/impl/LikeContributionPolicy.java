package com.project.contribution.policy.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.contribution.domain.support.ContributionScoreCodes;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LikeContributionPolicy implements ContributionPolicy {

    @Override
    public boolean supports(ActivityType activityType) {
        return activityType == ActivityType.LIKE_PRESSED || activityType == ActivityType.LIKE_CANCELLED;
    }

    @Override
    public List<ContributionPointCommand> evaluate(ActivityContext context) {
        return switch (context.activityType()) {
            case LIKE_PRESSED -> List.of(ContributionPointCommand.grant(
                    context.subjectUserId(),
                    ContributionScoreCodes.LIKE_RECEIVED,
                    context.referenceId(),
                    ActivityType.LIKE_PRESSED));
            case LIKE_CANCELLED -> List.of(ContributionPointCommand.revoke(
                    context.subjectUserId(),
                    ContributionScoreCodes.LIKE_RECEIVED,
                    context.referenceId(),
                    ActivityType.LIKE_CANCELLED,
                    ActivityType.LIKE_CANCELLED.name()));
            default -> List.of();
        };
    }
}
