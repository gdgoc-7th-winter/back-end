package com.project.user.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    /**
     * 세션이 없거나 비로그인이면 empty, 로그인되어 있으면 DB에 존재하는 사용자만 반환한다.
     */
    public Optional<User> findOptionalUser(HttpSession session) {
        if (session == null) {
            return Optional.empty();
        }
        UserSession userSession = (UserSession) session.getAttribute("LOGIN_USER");
        if (userSession == null) {
            return Optional.empty();
        }
        return userRepository.findById(userSession.getUserId());
    }
}
