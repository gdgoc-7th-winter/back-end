package com.project.contribution.application.dto;

import com.project.global.event.ActivityType;

import java.time.Instant;

public record ContributionPointCommand(
        Kind kind,
        long subjectUserId,
        String scoreCode,
        long referenceId,
        ActivityType activityType,
        String revokeReasonToken,
        Instant occurredAt
) {
    public enum Kind {
        GRANT,
        REVOKE
    }

    public static ContributionPointCommand grant(
            long subjectUserId,
            String scoreCode,
            long referenceId,
            ActivityType activityType,
            Instant occurredAt) {
        return new ContributionPointCommand(Kind.GRANT, subjectUserId, scoreCode, referenceId, activityType, null, occurredAt);
    }

    public static ContributionPointCommand revoke(
            long subjectUserId,
            String scoreCode,
            long referenceId,
            ActivityType activityType,
            String revokeReasonToken,
            Instant occurredAt) {
        return new ContributionPointCommand(
                Kind.REVOKE, subjectUserId, scoreCode, referenceId, activityType, revokeReasonToken, occurredAt);
    }
}
