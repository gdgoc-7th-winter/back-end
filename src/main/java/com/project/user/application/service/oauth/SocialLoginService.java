package com.project.user.application.service.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface SocialLoginService {

    void login(String provider, String code, String redirectUri, HttpServletRequest request, HttpServletResponse response);

    void connect(Long userId, String provider, String code, String redirectUri);
}
