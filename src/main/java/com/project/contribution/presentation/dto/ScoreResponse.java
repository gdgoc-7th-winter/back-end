package com.project.contribution.presentation.dto;

import com.project.contribution.domain.entity.ContributionScore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScoreResponse(
        @NotBlank String scoreName,
        @NotNull Integer point
) {
    public static ScoreResponse from(ContributionScore score) {
        return new ScoreResponse(
                score.getName(),
                score.getPoint()
        );
    }
}
