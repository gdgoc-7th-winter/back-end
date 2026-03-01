package com.project.global.interceptor;

import com.project.user.application.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class OnboardingInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    Logger log = LogManager.getLogger(OnboardingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        UserSession userSession = (UserSession) session.getAttribute("LOGIN_USER");

        if (userSession == null) {
            return true;
        }

        if (userSession.isNeedsProfile()) {
            // 프로필 설정 API 자체는 허용해야 무한 루프에 빠지지 않음
            if (requestURI.contains("/api/users/profile-setup")) {
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

            response.getWriter().write(jsonResponse);
            return false;
        }

        return true;
    }
}
