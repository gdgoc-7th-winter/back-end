package com.project.global.interceptor;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.domain.enums.Authority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlgoAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        Object attr = session.getAttribute("LOGIN_USER");
        if (!(attr instanceof UserSession userSession)) {
            return true;
        }

        if (userSession.getAuthority().equals(Authority.ADMIN)) {
            return true;
        }

        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        // 챌린지 목록/상세 조회는 허용 (풀이 목록·상세는 제외)
        if ("GET".equalsIgnoreCase(method)
                && !requestURI.contains("/answers")) {
            return true;
        }

        // 풀이 제출은 허용
        if ("POST".equalsIgnoreCase(method)
                && requestURI.matches("/api/v1/algo/\\d+/answers")) {
            return true;
        }

        log.warn("[AlgoAccessInterceptor] 권한 없는 접근 차단 - userId={}, authority={}, method={}, uri={}",
                userSession.getUserId(), userSession.getAuthority(), method, requestURI);
        throw new BusinessException(ErrorCode.ACCESS_DENIED, "권한이 없습니다.");
    }
}
