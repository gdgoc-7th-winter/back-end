package com.project.algo.application.dto;

import com.project.algo.domain.enums.ProgrammingLanguage;

import java.util.Objects;

public record ProgrammingLanguageResponse(
        ProgrammingLanguage language,
        String name,
        String syntaxMode,
        String fileExtension
) {
    public static ProgrammingLanguageResponse from(ProgrammingLanguage language) {
        Objects.requireNonNull(language, "language must not be null");
        return new ProgrammingLanguageResponse(
                language,
                language.getDisplayName(),
                language.getSyntaxMode(),
                language.getFileExtension()
        );
    }
}
