package com.project.user.presentation.dto.request;

public record LevelBadgeRequest(
        String levelName,
        String levelDescription,
        String levelImage,
        Integer minimumPoint,
        Integer maximumPoint
) {}
