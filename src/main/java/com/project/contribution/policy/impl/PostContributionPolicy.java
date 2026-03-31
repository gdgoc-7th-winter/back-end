package com.project.contribution.policy.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.contribution.domain.support.ContributionScoreCodes;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostContributionPolicy implements ContributionPolicy {

    @Override
    public boolean supports(ActivityType activityType) {
        return activityType == ActivityType.POST_CREATED || activityType == ActivityType.POST_DELETED;
    }

    @Override
    public List<ContributionPointCommand> evaluate(ActivityContext context) {
        return switch (context.activityType()) {
            case POST_CREATED -> List.of(ContributionPointCommand.grant(
                    context.subjectUserId(),
                    ContributionScoreCodes.POST_WRITE,
                    context.referenceId(),
                    ActivityType.POST_CREATED,
                    context.occurredAt()));
            case POST_DELETED -> List.of(ContributionPointCommand.revoke(
                    context.subjectUserId(),
                    ContributionScoreCodes.POST_WRITE,
                    context.referenceId(),
                    ActivityType.POST_DELETED,
                    ActivityType.POST_DELETED.name(),
                    context.occurredAt()));
            default -> List.of();
        };
    }
}
