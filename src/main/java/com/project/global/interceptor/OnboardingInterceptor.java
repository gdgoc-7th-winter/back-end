package com.project.global.interceptor;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
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
public class OnboardingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        Object attr = session.getAttribute("LOGIN_USER");

        if (!(attr instanceof UserSession userSession)) {
            return true;
        }

        if (userSession.isNeedsProfile()) {
            String method = request.getMethod();
            // 프로필 설정 API 및 설정에 필요한 조회 API는 허용
            if (requestURI.equals("/api/v1/users/profile-setup")
                    || requestURI.startsWith("/api/v1/departments")
                    || (requestURI.equals("/api/v1/me/profile") && "GET".equalsIgnoreCase(method))) {
                log.debug("[OnboardingInterceptor] 프로필 미설정 유저 허용된 엔드포인트 접근 통과 - userId={}, authority={}, method={}, uri={}",
                        userSession.getUserId(), userSession.getAuthority(), method, requestURI);
                return true;
            }
            log.warn("[OnboardingInterceptor] 프로필 미설정 유저 접근 차단 - userId={}, authority={}, method={}, uri={}",
                    userSession.getUserId(), userSession.getAuthority(), method, requestURI);
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "프로필 설정 후 이용하세요.");
        }

        return true;
    }
}
