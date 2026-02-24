package com.project.global.error;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), Instant.now());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message, Instant.now());
    }
}
