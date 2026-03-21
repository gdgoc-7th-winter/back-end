package com.project.user.presentation.controller;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.domain.enums.Authority;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuthConnectControllerTest {

    @Mock
    private HttpSession session;

    @InjectMocks
    private OAuthConnectController controller;

    @Test
    @DisplayName("로그인된 사용자가 소셜 연동 요청 시 LINK_USER_ID를 세션에 저장하고 OAuth 인가 페이지로 리다이렉트한다")
    void connectProviderSuccess() {
        UserSession sessionUser = UserSession.builder()
                .userId(1L)
                .authority(Authority.USER)
                .needsProfile(false)
                .build();
        given(session.getAttribute("LOGIN_USER")).willReturn(sessionUser);

        String result = controller.connectProvider("google");

        assertThat(result).isEqualTo("redirect:/oauth2/authorization/google");
        verify(session).setAttribute("LINK_USER_ID", 1L);
    }

    @Test
    @DisplayName("provider 문자열이 소문자로 정규화되어 리다이렉트 URL에 사용된다")
    void connectProviderNormalizesToLowerCase() {
        UserSession sessionUser = UserSession.builder()
                .userId(2L)
                .authority(Authority.USER)
                .needsProfile(false)
                .build();
        given(session.getAttribute("LOGIN_USER")).willReturn(sessionUser);

        String result = controller.connectProvider("KAKAO");

        assertThat(result).isEqualTo("redirect:/oauth2/authorization/kakao");
    }

    @Test
    @DisplayName("비로그인 상태에서 소셜 연동 요청 시 SESSION_NOT_FOUND 예외가 발생한다")
    void connectProviderWithoutLoginThrowsException() {
        given(session.getAttribute("LOGIN_USER")).willReturn(null);

        assertThatThrownBy(() -> controller.connectProvider("google"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
    }
}
