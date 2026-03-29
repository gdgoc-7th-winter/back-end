package com.project.algo.application.dto;

import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.Difficulty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DailyChallengeUpdateRequest(

        @Size(max = 300)
        String title,

        Difficulty difficulty,

        String description,
        String inputFormat,
        String outputFormat,

        @Size(max = 20)
        List<AlgorithmTag> algorithmTags
) {}
