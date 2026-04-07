package com.project.user.presentation.controller;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.response.CommonResponse;
import com.project.user.application.dto.OAuthCodeRequest;
import com.project.user.application.dto.UserSession;
import com.project.user.application.service.oauth.SocialLoginService;
import com.project.user.presentation.swagger.OAuthControllerDocs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
public class OAuthConnectController implements OAuthControllerDocs {

    private final SocialLoginService socialLoginService;
    private final HttpSession session;

    @PostMapping("/login/{provider}")
    public ResponseEntity<CommonResponse<Void>> loginWithProvider(
            @PathVariable @NonNull String provider,
            @RequestBody @Valid @NonNull OAuthCodeRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        log.info("소셜 로그인 요청 - provider: {}", provider);
        socialLoginService.login(provider, request.code(), request.redirectUri(), servletRequest, servletResponse);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @PostMapping("/connect/{provider}")
    public ResponseEntity<CommonResponse<Void>> connectProvider(
            @PathVariable @NonNull String provider,
            @RequestBody @Valid @NonNull OAuthCodeRequest request) {

        UserSession sessionUser = (UserSession) session.getAttribute("LOGIN_USER");
        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        log.info("소셜 계정 연동 요청 - userId: {}, provider: {}", sessionUser.getUserId(), provider);
        socialLoginService.connect(sessionUser.getUserId(), provider, request.code(), request.redirectUri());
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
