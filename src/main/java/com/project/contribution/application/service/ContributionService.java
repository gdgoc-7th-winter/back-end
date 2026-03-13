package com.project.contribution.application.service;

import com.project.global.event.ActivityType;

public interface ContributionService {
    public void checkAndGrantScores(Long userId, ActivityType activityType);
    public void grantScore(Long userId, String scoreName);
}
