package com.project.contribution.policy.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.event.ActivityType;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class SystemScoreGrantPolicy implements ContributionPolicy {

    @Override
    public boolean supports(ActivityType activityType) {
        return activityType == ActivityType.SYSTEM_SCORE_GRANT;
    }

    @Override
    public List<ContributionPointCommand> evaluate(ActivityContext context) {
        if (context.activityType() != ActivityType.SYSTEM_SCORE_GRANT) {
            return List.of();
        }
        if (!StringUtils.hasText(context.scoreCodeOverride())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "SYSTEM_SCORE_GRANT에는 scoreCode가 필요합니다.");
        }
        return List.of(ContributionPointCommand.grant(
                context.subjectUserId(),
                context.scoreCodeOverride(),
                context.referenceId(),
                ActivityType.SYSTEM_SCORE_GRANT,
                context.occurredAt()));
    }
}
