package com.project.global.error;

import org.springframework.lang.Nullable;

import java.util.Objects;

public final class ErrorResponses {

    private ErrorResponses() {
    }

    public static ErrorResponse clientError(ErrorCode errorCode, @Nullable String message) {
        Objects.requireNonNull(errorCode, "errorCode");
        String resolved = (message == null || message.isBlank()) ? errorCode.getMessage() : message;
        return ErrorResponse.of(errorCode, resolved);
    }

    public static ErrorResponse serverError(ErrorCode errorCode) {
        return ErrorResponse.of(Objects.requireNonNull(errorCode, "errorCode"));
    }
}
