package com.project.contribution.application.service;

import com.project.global.event.ActivityType;
import com.project.user.application.dto.EarnScoreResult;

import org.springframework.lang.Nullable;

public interface ContributionCommandService {

    EarnScoreResult grantScore(Long userId, String scoreCode, Long referenceId);

    EarnScoreResult grantScore(Long userId, String scoreCode, Long referenceId, @Nullable ActivityType activityType);

    EarnScoreResult revokeScore(
            Long userId,
            String scoreCode,
            Long referenceId,
            ActivityType activityType,
            String revokeReasonToken);
}
