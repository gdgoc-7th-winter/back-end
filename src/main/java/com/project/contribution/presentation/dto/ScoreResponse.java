package com.project.contribution.presentation.dto;

import com.project.contribution.domain.entity.ContributionScore;

public record ScoreResponse(
        String scoreName,
        Integer point
) {
    public static ScoreResponse from(ContributionScore score) {
        return new ScoreResponse(
                score.getName(),
                score.getPoint()
        );
    }
}
