package com.project.contribution.application.service;

import com.project.global.event.ActivityType;
import com.project.user.application.dto.EarnScoreResult;

import org.springframework.lang.Nullable;

import java.time.Instant;

public interface ContributionCommandService {

    EarnScoreResult grantScore(Long userId, String scoreCode, Long referenceId);

    EarnScoreResult grantScore(Long userId, String scoreCode, Long referenceId, @Nullable ActivityType activityType);

    EarnScoreResult grantScore(
            Long userId,
            String scoreCode,
            Long referenceId,
            @Nullable ActivityType activityType,
            @Nullable Instant occurredAt);

    EarnScoreResult revokeScore(
            Long userId,
            String scoreCode,
            Long referenceId,
            ActivityType activityType,
            String revokeReasonToken);

    EarnScoreResult revokeScore(
            Long userId,
            String scoreCode,
            Long referenceId,
            ActivityType activityType,
            String revokeReasonToken,
            @Nullable Instant occurredAt);
}
