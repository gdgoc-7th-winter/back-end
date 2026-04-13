package com.project.contribution.policy;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.global.event.ActivityType;

import java.util.List;

public interface ContributionPolicy {

    boolean supports(ActivityType activityType);

    List<ContributionPointCommand> evaluate(ActivityContext context);
}
