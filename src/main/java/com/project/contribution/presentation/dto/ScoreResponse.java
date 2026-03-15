package com.project.contribution.presentation.dto;

import com.project.contribution.domain.entity.ContributionScore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record ScoreResponse(
        @NotBlank String scoreName,
        @NotNull Integer point
) {
    public static ScoreResponse from(ContributionScore score) {
        Objects.requireNonNull(score, "contribution score is null");
        return new ScoreResponse(
                score.getName(),
                score.getPoint()
        );
    }
}
