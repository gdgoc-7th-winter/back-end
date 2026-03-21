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
    DUPLICATED_ADDRESS(HttpStatus.CONFLICT, "E003", "이미 존재하는 아이디입니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A001", "이메일 또는 비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "A002", "현재 비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "A004", "세션이 만료되었습니다."),
    OAUTH_PROVIDER_ERROR(HttpStatus.UNAUTHORIZED, "A005", "OAuth 제공자 응답에서 필수 정보를 가져올 수 없습니다."),

    // File
    FILE_NOT_FOUND_IN_S3(HttpStatus.BAD_REQUEST, "F001", "S3에 해당 파일이 존재하지 않습니다. 업로드를 먼저 완료해주세요."),
    INVALID_OBJECT_KEY(HttpStatus.BAD_REQUEST, "F002", "objectKey 형식이 올바르지 않습니다."),
    FILE_METADATA_MISMATCH(HttpStatus.BAD_REQUEST, "F006", "요청한 파일 정보와 S3에 저장된 파일이 일치하지 않습니다."),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "F003", "허용되지 않은 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F004", "파일 크기가 제한을 초과했습니다."),
    UNSUPPORTED_UPLOAD_TYPE(HttpStatus.BAD_REQUEST, "F005", "지원하지 않는 업로드 타입입니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;
}
