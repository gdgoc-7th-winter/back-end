package com.project.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CSRF 실패 시 {@link MissingCsrfTokenException} / {@link InvalidCsrfTokenException}을 구분해 로그에 남긴다.
 * 그 외 접근 거부는 기본 {@link AccessDeniedHandlerImpl} 동작을 유지한다.
 */
@Slf4j
@Component
public class CsrfAwareAccessDeniedHandler implements AccessDeniedHandler {

    private final AccessDeniedHandler delegate = new AccessDeniedHandlerImpl();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                         AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (accessDeniedException instanceof MissingCsrfTokenException) {
            // CsrfFilter: deferredCsrfToken.isGenerated()==true 인 경우 — 비교할 기대 토큰이 없음(쿠키/세션에 없음 등)
            log.warn("CSRF 403: MissingCsrfTokenException — no expected token to compare method={} uri={}",
                    request.getMethod(), request.getRequestURI());
        } else if (accessDeniedException instanceof InvalidCsrfTokenException) {
            // CsrfFilter: 기대 토큰은 있으나 X-XSRF-TOKEN(또는 파라미터) 값이 일치하지 않음
            log.warn("CSRF 403: InvalidCsrfTokenException — token mismatch (check X-XSRF-TOKEN vs XSRF-TOKEN cookie) method={} uri={}",
                    request.getMethod(), request.getRequestURI());
        }
        delegate.handle(request, response, accessDeniedException);
    }
}
