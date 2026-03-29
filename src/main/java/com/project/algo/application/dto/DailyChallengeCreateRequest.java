package com.project.algo.application.dto;

import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.CodingTestSite;
import com.project.algo.domain.enums.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DailyChallengeCreateRequest(

        @NotBlank(message = "문제 제목은 필수입니다.")
        @Size(max = 300)
        String title,

        @NotNull(message = "코딩 테스트 사이트는 필수입니다.")
        CodingTestSite sourceSite,

        @NotBlank(message = "문제 번호는 필수입니다.")
        @Size(max = 50)
        String problemNumber,

        Difficulty difficulty,

        @Size(max = 20)
        List<AlgorithmTag> algorithmTags,

        @NotBlank(message = "원본 문제 링크는 필수입니다.")
        String originalUrl,

        String description,
        String inputFormat,
        String outputFormat
) {}
