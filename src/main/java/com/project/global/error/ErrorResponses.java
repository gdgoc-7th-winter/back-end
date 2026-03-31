package com.project.global.error;

import org.springframework.lang.Nullable;

import java.util.Objects;

public final class ErrorResponses {

    private ErrorResponses() {
    }

    // message는 비즈니스·검증용 문구만 넣는다(원문 예외 문자열·내부 스택 금지)
    public static ErrorResponse clientError(ErrorCode errorCode, @Nullable String message) {
        Objects.requireNonNull(errorCode, "errorCode");
        String resolved = (message == null || message.isBlank()) ? errorCode.getMessage() : message;
        return ErrorResponse.of(errorCode, resolved);
    }

    public static ErrorResponse serverError(ErrorCode errorCode) {
        return ErrorResponse.of(Objects.requireNonNull(errorCode, "errorCode"));
    }
}
