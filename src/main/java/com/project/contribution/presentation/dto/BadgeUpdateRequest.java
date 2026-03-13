package com.project.contribution.presentation.dto;

public record BadgeUpdateRequest(
        String badgeName,
        String badgeDescription,
        String badgeImage,
        Integer point
) {}
