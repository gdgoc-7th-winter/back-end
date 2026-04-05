package com.project.contribution.policy.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.contribution.domain.support.ContributionScoreCodes;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScrapContributionPolicy implements ContributionPolicy {

    @Override
    public boolean supports(ActivityType activityType) {
        return activityType == ActivityType.SCRAP_PRESSED;
    }

    @Override
    public List<ContributionPointCommand> evaluate(ActivityContext context) {
        if (context.activityType() != ActivityType.SCRAP_PRESSED) {
            return List.of();
        }
        return List.of(ContributionPointCommand.grant(
                context.subjectUserId(),
                ContributionScoreCodes.SCRAP_RECEIVED,
                context.referenceId(),
                ActivityType.SCRAP_PRESSED,
                context.occurredAt()));
    }
}
