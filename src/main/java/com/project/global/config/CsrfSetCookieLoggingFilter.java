package com.project.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * {@link org.springframework.security.web.csrf.CsrfFilter}가 응답에 {@code XSRF-TOKEN} Set-Cookie를 붙일 때 INFO 로그를 남긴다.
 * 로컬(host-only) / 운영({@code Domain=.hufs.dev}) 모두 동일하게 “발급 여부”만 확인할 수 있다.
 */
@Slf4j
public class CsrfSetCookieLoggingFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "XSRF-TOKEN";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletResponse wrapped = new HttpServletResponseWrapper(response) {
            @Override
            public void addHeader(String name, String value) {
                super.addHeader(name, value);
                logIfXsrfSetCookie(name, value);
            }

            @Override
            public void setHeader(String name, String value) {
                super.setHeader(name, value);
                logIfXsrfSetCookie(name, value);
            }

            @Override
            public void addCookie(Cookie cookie) {
                super.addCookie(cookie);
                if (cookie != null && COOKIE_NAME.equals(cookie.getName())) {
                    log.info("CSRF: {} cookie issued (Set-Cookie via Cookie) uri={}", COOKIE_NAME, request.getRequestURI());
                }
            }

            private void logIfXsrfSetCookie(String name, String value) {
                if (!"Set-Cookie".equalsIgnoreCase(name) || value == null) {
                    return;
                }
                String v = value.trim();
                if (v.regionMatches(true, 0, COOKIE_NAME, 0, COOKIE_NAME.length())) {
                    log.info("CSRF: {} cookie issued (Set-Cookie header) uri={}", COOKIE_NAME, request.getRequestURI());
                }
            }
        };
        filterChain.doFilter(request, wrapped);
    }
}
