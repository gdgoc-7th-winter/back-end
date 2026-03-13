package com.project.contribution.presentation.dto;

public record BadgeCreateRequest(
        String badgeName,
        String badgeDescription,
        String badgeImage,
        Integer point
) {}
