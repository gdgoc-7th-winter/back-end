package com.project.user.application.service.oauth;

import jakarta.servlet.http.HttpServletRequest;

public interface SocialLoginService {

    void login(String provider, String code, String redirectUri, HttpServletRequest request);

    void connect(Long userId, String provider, String code, String redirectUri);
}
