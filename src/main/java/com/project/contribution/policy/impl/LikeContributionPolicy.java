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
        return activityType == ActivityType.LIKE_PRESSED;
    }

    @Override
    public List<ContributionPointCommand> evaluate(ActivityContext context) {
        if (context.activityType() != ActivityType.LIKE_PRESSED) {
            return List.of();
        }
        return List.of(ContributionPointCommand.grant(
                context.subjectUserId(),
                ContributionScoreCodes.LIKE_RECEIVED,
                context.referenceId(),
                ActivityType.LIKE_PRESSED,
                context.occurredAt()));
    }
}
