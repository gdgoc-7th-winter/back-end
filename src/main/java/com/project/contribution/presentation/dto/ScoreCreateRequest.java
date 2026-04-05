package com.project.contribution.presentation.dto;

public record ScoreCreateRequest(
        String scoreCode,
        String scoreName,
        Integer point
) {}
