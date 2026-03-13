package com.project.contribution.application.service;

import com.project.global.event.ActivityType;

public interface ContributionService {
    public void checkAndGrantBadges(Long userId, ActivityType activityType);
    public void grantBadge(Long userId, String badgeName);
}
