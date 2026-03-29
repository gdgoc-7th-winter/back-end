package com.project.algo.application.dto;

import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.ProgrammingLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AnswerCodePostCreateRequest(

        @NotNull(message = "언어는 필수입니다.")
        ProgrammingLanguage language,

        @NotBlank(message = "코드는 필수입니다.")
        String code,

        @NotBlank(message = "풀이 설명은 필수입니다.")
        @Size(max = 50_000)
        String explanation,

        @Size(max = 100)
        String timeComplexity,

        @PositiveOrZero
        Integer runtime,

        @Size(max = 20)
        List<AlgorithmTag> algorithmTags
) {}
