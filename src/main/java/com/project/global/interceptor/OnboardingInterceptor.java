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

import java.io.IOException;

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
                    || requestURI.startsWith("/api/v1/departments")) {
                return true;
            }

            // 4. JSON 응답 반환 (REST API 방식)
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden

            String jsonResponse = "{"
                    + "\"success\": false,"
                    + "\"data\": null,"
                    + "\"message\": \"프로필 설정을 먼저 완료해주세요.\""
                    + "}";

            try {
                response.getWriter().write(jsonResponse);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            return false;
        }

        return true;
    }
}
