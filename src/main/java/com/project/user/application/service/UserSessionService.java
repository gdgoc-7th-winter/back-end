package com.project.user.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserRepository userRepository;

    public User getCurrentUser(HttpSession session) {
        if (session == null) {
            throw new BusinessException(ErrorCode.SESSION_EXPIRED, "로그인이 필요합니다.");
        }
        UserSession userSession = (UserSession) session.getAttribute("LOGIN_USER");
        if (userSession == null) {
            throw new BusinessException(ErrorCode.SESSION_EXPIRED, "로그인이 필요합니다.");
        }
        return userRepository.findById(userSession.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
