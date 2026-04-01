package com.project.contribution.policy;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.global.event.ActivityType;

import java.util.List;

/**
 * 활동 맥락에 따라 지급·회수 명령 목록을 반환한다 (§24.7). DB에 직접 쓰지 않는다.
 */
public interface ContributionPolicy {

    boolean supports(ActivityType activityType);

    List<ContributionPointCommand> evaluate(ActivityContext context);
}
