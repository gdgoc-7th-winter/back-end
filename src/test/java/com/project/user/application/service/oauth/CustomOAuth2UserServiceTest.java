package com.project.user.application.service.oauth;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;
import com.project.user.domain.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private HttpSession session;
    @Mock private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private MockHttpServletRequest request;
    private MockHttpSession mockSession;
    private OAuth2UserRequest userRequest;
    private OAuth2User mockOAuth2User;
    private User user;

    private static final String PROVIDER_ID = "google-12345";
    private static final String EMAIL = "user@google.com";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        mockSession = new MockHttpSession();
        request.setSession(mockSession);

        ClientRegistration registration = ClientRegistration
                .withRegistrationId("google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .redirectUri("{baseUrl}/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "test-token",
                Instant.now(), Instant.now().plusSeconds(3600));

        userRequest = new OAuth2UserRequest(registration, accessToken);

        Map<String, Object> attributes = Map.of("sub", PROVIDER_ID, "email", EMAIL);
        mockOAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("USER")), attributes, "sub");

        user = new User(EMAIL, "encodedPw", "nickname");
        ReflectionTestUtils.setField(user, "id", USER_ID);

        // 필드로 선언된 delegate를 mock으로 교체
        ReflectionTestUtils.setField(customOAuth2UserService, "delegate", delegate);
    }

    @Test
    @DisplayName("로그인 케이스 - 연동된 계정이 존재하면 세션에 유저 정보를 저장하고 DefaultOAuth2User를 반환한다")
    void loadUserLoginWithLinkedAccount() {
        try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
            mockedContext.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));

            given(delegate.loadUser(any())).willReturn(mockOAuth2User);
            given(session.getAttribute("LINK_USER_ID")).willReturn(null);
            given(userRepository.findByProviderAndProviderId("google", PROVIDER_ID)).willReturn(Optional.of(user));

            OAuth2User result = customOAuth2UserService.loadUser(userRequest);

            assertThat(result).isInstanceOf(DefaultOAuth2User.class);
            UserSession userSession = (UserSession) mockSession.getAttribute("LOGIN_USER");
            assertThat(userSession).isNotNull();
            assertThat(userSession.getUserId()).isEqualTo(USER_ID);
            assertThat(userSession.getAuthority()).isEqualTo(Authority.DUMMY);
        }
    }

    @Test
    @DisplayName("로그인 케이스 - 연동된 계정이 없으면 OAuth2AuthenticationException이 발생한다")
    void loadUserLoginWithNoLinkedAccount() {
        given(delegate.loadUser(any())).willReturn(mockOAuth2User);
        given(session.getAttribute("LINK_USER_ID")).willReturn(null);
        given(userRepository.findByProviderAndProviderId("google", PROVIDER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(
                        org.springframework.security.oauth2.core.OAuth2AuthenticationException.class);

        verify(userService, never()).linkSocialAccount(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("계정 연동 케이스 - linkSocialAccount 호출 후 세션에서 LINK_USER_ID가 제거된다")
    void loadUserLinkAccountSuccess() {
        given(delegate.loadUser(any())).willReturn(mockOAuth2User);
        given(session.getAttribute("LINK_USER_ID")).willReturn(USER_ID);

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        verify(userService).linkSocialAccount(
                eq(USER_ID), eq("GOOGLE"), eq(EMAIL), eq(PROVIDER_ID));
        verify(session).removeAttribute("LINK_USER_ID");
        assertThat(result).isSameAs(mockOAuth2User);
    }

    @Test
    @DisplayName("계정 연동 케이스 - 이미 연동된 계정이면 BusinessException이 OAuth2AuthenticationException으로 변환된다")
    void loadUserLinkDuplicateAccountThrowsOAuthException() {
        given(delegate.loadUser(any())).willReturn(mockOAuth2User);
        given(session.getAttribute("LINK_USER_ID")).willReturn(USER_ID);
        willThrow(new BusinessException(ErrorCode.DUPLICATED_ADDRESS, "이미 등록하신 소셜 로그인 계정입니다."))
                .given(userService)
                .linkSocialAccount(any(), anyString(), anyString(), anyString());

        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(
                        org.springframework.security.oauth2.core.OAuth2AuthenticationException.class);

        verify(session, never()).removeAttribute("LINK_USER_ID");
    }
}
