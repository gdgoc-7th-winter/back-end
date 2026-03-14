package com.project.contribution.presentation.dto;

import com.project.contribution.domain.entity.ContributionScore;
import jakarta.validation.constraints.NotBlank;

public record ScoreResponse(
        @NotBlank String scoreName,
        @NotBlank Integer point
) {
    public static ScoreResponse from(ContributionScore score) {
        return new ScoreResponse(
                score.getName(),
                score.getPoint()
        );
    }
}
