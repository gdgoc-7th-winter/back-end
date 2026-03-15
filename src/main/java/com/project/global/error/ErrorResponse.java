package com.project.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        List<ValidationError> errors
) {

    public record ValidationError(String field, String reason) {
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), Instant.now(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message, Instant.now(), null);
    }

    public static ErrorResponse validationError(String code, String message, List<ValidationError> errors) {
        return new ErrorResponse(code, message, Instant.now(), errors);
    }
}
