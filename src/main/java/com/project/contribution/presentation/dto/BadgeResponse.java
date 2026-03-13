package com.project.contribution.presentation.dto;

import com.project.contribution.domain.entity.ContributionBadge;

public record BadgeResponse(
        String badgeName,
        String badgeDescription,
        String badgeImage,
        Integer point
) {
    public static BadgeResponse from(ContributionBadge badge) {
        return new BadgeResponse(
                badge.getName(),
                badge.getBadgeDescription(),
                badge.getBadgeImage(),
                badge.getPoint()
        );
    }
}
