package com.project.user.domain.repository.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
public class CustomAuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String ATTRIBUTE_NAME = "OAUTH2_AUTHORIZATION_REQUEST";

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (OAuth2AuthorizationRequest) session.getAttribute(ATTRIBUTE_NAME);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(ATTRIBUTE_NAME, request);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.debug("OAuth2 인가 요청 세션 없음");
            return null;
        }

        OAuth2AuthorizationRequest requestData = (OAuth2AuthorizationRequest) session.getAttribute(ATTRIBUTE_NAME);
        if (requestData != null) {
            session.removeAttribute(ATTRIBUTE_NAME);
            log.debug("OAuth2 인가 요청 세션 로드 완료 - sessionId: {}", session.getId());
        }
        return requestData;
    }
}
