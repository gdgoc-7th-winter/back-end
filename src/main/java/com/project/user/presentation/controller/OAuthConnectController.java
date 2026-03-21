package com.project.user.presentation.controller;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.presentation.swagger.OAuthControllerDocs;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
public class OAuthConnectController implements OAuthControllerDocs {
    private final HttpSession session;

    @GetMapping("/login/{provider}")
    public String loginWithProvider(@PathVariable("provider") String provider) {
        log.info("OAuth2 로그인 시작 - provider: {}", provider);
        return "redirect:/oauth2/authorization/" + provider.toLowerCase();
    }

    @GetMapping("/connect/{provider}")
    public String connectProvider(@PathVariable("provider") String provider) {
        UserSession sessionUser = (UserSession) session.getAttribute("LOGIN_USER");

        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        session.setAttribute("LINK_USER_ID", sessionUser.getUserId());
        log.info("소셜 계정 연동 시작 - userId: {}, provider: {}", sessionUser.getUserId(), provider);

        return "redirect:/oauth2/authorization/" + provider.toLowerCase();
    }
}
