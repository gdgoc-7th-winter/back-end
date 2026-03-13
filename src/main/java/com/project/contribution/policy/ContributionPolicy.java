package com.project.contribution.policy;

import com.project.contribution.domain.entity.ContributionBadge;
import com.project.global.event.ActivityType;

public interface ContributionPolicy {
    boolean supports(ActivityType activityType);
    boolean isSatisfied(Long userId);
    ContributionBadge getBadge();
}
