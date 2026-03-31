package com.project.contribution.policy.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.contribution.domain.support.ContributionScoreCodes;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentContributionPolicy implements ContributionPolicy {

    @Override
    public boolean supports(ActivityType activityType) {
        return activityType == ActivityType.COMMENT_WRITTEN || activityType == ActivityType.COMMENT_DELETED;
    }

    @Override
    public List<ContributionPointCommand> evaluate(ActivityContext context) {
        return switch (context.activityType()) {
            case COMMENT_WRITTEN -> List.of(ContributionPointCommand.grant(
                    context.subjectUserId(),
                    ContributionScoreCodes.COMMENT_WRITE,
                    context.referenceId(),
                    ActivityType.COMMENT_WRITTEN));
            case COMMENT_DELETED -> List.of(ContributionPointCommand.revoke(
                    context.subjectUserId(),
                    ContributionScoreCodes.COMMENT_WRITE,
                    context.referenceId(),
                    ActivityType.COMMENT_DELETED,
                    ActivityType.COMMENT_DELETED.name()));
            default -> List.of();
        };
    }
}
