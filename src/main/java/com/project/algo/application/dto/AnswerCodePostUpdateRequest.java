package com.project.algo.application.dto;

import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.ProgrammingLanguage;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AnswerCodePostUpdateRequest(

        ProgrammingLanguage language,

        String code,

        @Size(max = 50_000)
        String explanation,

        @Size(max = 100)
        String timeComplexity,

        @PositiveOrZero
        Integer runtime,

        @Size(max = 20)
        List<AlgorithmTag> algorithmTags
) {}
