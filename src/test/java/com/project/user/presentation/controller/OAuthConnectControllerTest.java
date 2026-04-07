package com.project.user.presentation.controller;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.OAuthCodeRequest;
import com.project.user.application.dto.UserSession;
import com.project.user.application.service.oauth.SocialLoginService;
import com.project.user.domain.enums.Authority;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuthConnectControllerTest {

    @Mock private SocialLoginService socialLoginService;
    @Mock private HttpSession session;

    @InjectMocks
    private OAuthConnectController controller;

    private static final String CODE         = "test-auth-code";
    private static final String REDIRECT_URI = "https://www.hufs.dev/oauth2/callback/kakao";

    @Test
    @DisplayName("소셜 로그인 요청 시 SocialLoginService.login()이 올바른 인자로 호출되고 200을 반환한다")
    void loginWithProviderSuccess() {
        OAuthCodeRequest request = new OAuthCodeRequest(CODE, REDIRECT_URI);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        var response = controller.loginWithProvider("kakao", request, servletRequest, servletResponse);

        verify(socialLoginService).login("kakao", CODE, REDIRECT_URI, servletRequest, servletResponse);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("provider 문자열은 서비스에 그대로 전달된다 (대소문자 변환은 서비스 책임)")
    void loginWithProviderPassesProviderAsIs() {
        OAuthCodeRequest request = new OAuthCodeRequest(CODE, REDIRECT_URI);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        controller.loginWithProvider("KAKAO", request, servletRequest, servletResponse);

        verify(socialLoginService).login("KAKAO", CODE, REDIRECT_URI, servletRequest, servletResponse);
    }

    @Test
    @DisplayName("로그인 상태에서 소셜 연동 요청 시 SocialLoginService.connect()가 호출되고 200을 반환한다")
    void connectProviderSuccess() {
        UserSession sessionUser = UserSession.builder()
                .userId(1L)
                .authority(Authority.USER)
                .needsProfile(false)
                .build();
        given(session.getAttribute("LOGIN_USER")).willReturn(sessionUser);
        OAuthCodeRequest request = new OAuthCodeRequest(CODE, REDIRECT_URI);

        var response = controller.connectProvider("google", request);

        verify(socialLoginService).connect(1L, "google", CODE, REDIRECT_URI);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("비로그인 상태에서 소셜 연동 요청 시 SESSION_NOT_FOUND 예외가 발생한다")
    void connectProviderWithoutLoginThrowsException() {
        given(session.getAttribute("LOGIN_USER")).willReturn(null);
        OAuthCodeRequest request = new OAuthCodeRequest(CODE, REDIRECT_URI);

        assertThatThrownBy(() -> controller.connectProvider("google", request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
    }
}
