package com.project.algo.application.dto;

import com.project.algo.domain.enums.ProgrammingLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CodeRunRequest(

        @NotNull(message = "언어는 필수입니다.")
        ProgrammingLanguage language,

        @NotBlank(message = "코드는 필수입니다.")
        String code,

        /** 표준 입력값 (stdin). null이면 빈 문자열로 처리. */
        String stdin
) {}
