package com.project.contribution.policy.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.contribution.domain.support.ContributionScoreCodes;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewFacePolicy implements ContributionPolicy {

    @Override
    public boolean supports(ActivityType activityType) {
        return activityType == ActivityType.PROFILE_SETUP_COMPLETED;
    }

    @Override
    public List<ContributionPointCommand> evaluate(ActivityContext context) {
        if (context.activityType() != ActivityType.PROFILE_SETUP_COMPLETED) {
            return List.of();
        }
        return List.of(ContributionPointCommand.grant(
                context.subjectUserId(),
                ContributionScoreCodes.NEW_FACE,
                context.referenceId(),
                context.activityType(),
                context.occurredAt()));
    }
}
