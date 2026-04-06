package com.project.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 기동 시 CSRF 쿠키 정책(도메인 host-only vs registrable domain)을 한 줄로 남겨
 * 로컬/운영 프로파일에서 기대값과 일치하는지 확인하기 쉽게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsrfCookieStartupLogger {

    private final CsrfCookieProperties csrfCookieProperties;

    @PostConstruct
    void logCsrfCookiePolicy() {
        if (csrfCookieProperties.hasDomain()) {
            log.info("CSRF cookie policy: Domain={} (Set-Cookie value), secure={}, SameSite={}, path={}",
                    csrfCookieProperties.domainForSetCookie(),
                    csrfCookieProperties.secure(),
                    csrfCookieProperties.sameSite(),
                    csrfCookieProperties.path());
        } else {
            log.info("CSRF cookie policy: host-only (no Domain attribute), secure={}, SameSite={}, path={}",
                    csrfCookieProperties.secure(),
                    csrfCookieProperties.sameSite(),
                    csrfCookieProperties.path());
        }
    }
}
