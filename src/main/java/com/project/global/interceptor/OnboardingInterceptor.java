package com.project.global.interceptor;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
            // 프로필 설정 API 및 설정에 필요한 조회 API는 허용
            if (requestURI.equals("/api/v1/users/profile-setup")
                    || requestURI.startsWith("/api/v1/departments")
                    || requestURI.startsWith("/api/v1/me/profile")) {
                return true;
            }
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return true;
    }
}
