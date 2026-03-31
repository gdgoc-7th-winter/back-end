package com.project.contribution.application.dto;

import com.project.global.event.ActivityType;

public record ContributionPointCommand(
        Kind kind,
        long subjectUserId,
        String scoreCode,
        long referenceId,
        ActivityType activityType,
        String revokeReasonToken
) {
    public enum Kind {
        GRANT,
        REVOKE
    }

    public static ContributionPointCommand grant(
            long subjectUserId,
            String scoreCode,
            long referenceId,
            ActivityType activityType) {
        return new ContributionPointCommand(Kind.GRANT, subjectUserId, scoreCode, referenceId, activityType, null);
    }

    public static ContributionPointCommand revoke(
            long subjectUserId,
            String scoreCode,
            long referenceId,
            ActivityType activityType,
            String revokeReasonToken) {
        return new ContributionPointCommand(
                Kind.REVOKE, subjectUserId, scoreCode, referenceId, activityType, revokeReasonToken);
    }
}
