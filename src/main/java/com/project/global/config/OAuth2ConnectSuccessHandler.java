package com.project.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2ConnectSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String LOGIN_SUCCESS_URL = "/api/v1/me/profile";
    private static final String LINK_SUCCESS_URL = "/api/v1/me/profile";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null && "link".equals(session.getAttribute("OAUTH2_OPERATION"))) {
            session.removeAttribute("OAUTH2_OPERATION");
            getRedirectStrategy().sendRedirect(request, response, LINK_SUCCESS_URL);
        } else {
            getRedirectStrategy().sendRedirect(request, response, LOGIN_SUCCESS_URL);
        }
    }
}
