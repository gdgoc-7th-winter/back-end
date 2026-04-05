package com.project.global.error;

import com.project.post.domain.exception.PostDomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus().value())
                .body(ErrorResponses.clientError(errorCode, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e
    ) {
        log.warn("Validation failed: {}", e.getMessage());
        List<ErrorResponse.ValidationError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        String message = e.getBindingResult().getAllErrors().isEmpty()
                ? ErrorCode.INVALID_INPUT.getMessage()
                : e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.validationError(ErrorCode.INVALID_INPUT.getCode(), message, errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException: {}", e.getMessage());
        String message = e.getMessage() != null && e.getMessage().contains("uk_users_nickname")
                ? "이미 사용 중인 닉네임입니다."
                : "이미 존재하는 데이터입니다.";
        return ResponseEntity
                .status(ErrorCode.DUPLICATED_ADDRESS.getStatus().value())
                .body(ErrorResponses.clientError(ErrorCode.DUPLICATED_ADDRESS, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponses.clientError(ErrorCode.INVALID_INPUT, e.getMessage()));
    }

    @ExceptionHandler(PostDomainException.class)
    protected ResponseEntity<ErrorResponse> handlePostDomainException(PostDomainException e) {
        log.warn("PostDomainException: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponses.clientError(ErrorCode.INVALID_INPUT, e.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e
    ) {
        log.warn("Method not allowed: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getStatus().value())
                .body(ErrorResponses.clientError(
                        ErrorCode.METHOD_NOT_ALLOWED,
                        ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception: ", e);
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponses.serverError(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
