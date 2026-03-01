package com.project.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //BAD_REQUEST
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "허용되지 않은 HTTP 메서드입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "C005","요청 허용 횟수를 초과했습니다."),

    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "E001", "인증번호가 다릅니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "E002", "세션 정보가 만료되었거나 찾을 수 없습니다."),
    DUPLICATED_ADDRESS(HttpStatus.CONFLICT, "E003", "이미 존재하는 아이디입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
