package com.project.algo.application.dto;

import com.project.algo.domain.enums.ProgrammingLanguage;

public record ProgrammingLanguageResponse(
        ProgrammingLanguage language,
        String name,
        String syntaxMode,
        String fileExtension
) {
    public static ProgrammingLanguageResponse from(ProgrammingLanguage language) {
        return new ProgrammingLanguageResponse(
                language,
                language.getDisplayName(),
                language.getSyntaxMode(),
                language.getFileExtension()
        );
    }
}
