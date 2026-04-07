package com.project.user.application.service.oauth;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.OAuthAttributes;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SocialLoginServiceImplTest {

    @Mock private OAuth2HttpClient oauth2HttpClient;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;

    @InjectMocks
    private SocialLoginServiceImpl service;

    private static final String PROVIDER     = "kakao";
    private static final String CODE         = "test-auth-code";
    private static final String REDIRECT_URI = "https://www.hufs.dev/oauth2/callback/kakao";
    private static final String PROVIDER_ID  = "12345";
    private static final String EMAIL        = "user@kakao.com";
    private static final Long   USER_ID      = 1L;

    private OAuthAttributes kakaoAttrs;
    private User user;

    @BeforeEach
    void setUp() {
        kakaoAttrs = new OAuthAttributes(Map.of("id", PROVIDER_ID), "id", PROVIDER_ID, EMAIL);

        user = User.builder().email(EMAIL).password("pw").nickname("nick").build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        ReflectionTestUtils.setField(user, "authority", Authority.USER);
    }

    // ── login() ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("로그인 - 연동된 계정이 있으면 세션에 UserSession이 저장된다")
    void loginSuccess() {
        given(oauth2HttpClient.fetchUserInfo(PROVIDER, CODE, REDIRECT_URI)).willReturn(kakaoAttrs);
        given(userRepository.findByProviderAndProviderId("KAKAO", PROVIDER_ID)).willReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        service.login(PROVIDER, CODE, REDIRECT_URI, request, new MockHttpServletResponse());

        HttpSession session = request.getSession(false);
        assertThat(session).isNotNull();

        UserSession userSession = (UserSession) session.getAttribute("LOGIN_USER");
        assertThat(userSession).isNotNull();
        assertThat(userSession.getUserId()).isEqualTo(USER_ID);
        assertThat(userSession.getAuthority()).isEqualTo(Authority.USER);
    }

    @Test
    @DisplayName("로그인 - 연동된 계정이 없으면 OAUTH_PROVIDER_ERROR 예외가 발생한다")
    void loginNoLinkedAccount() {
        given(oauth2HttpClient.fetchUserInfo(PROVIDER, CODE, REDIRECT_URI)).willReturn(kakaoAttrs);
        given(userRepository.findByProviderAndProviderId("KAKAO", PROVIDER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(PROVIDER, CODE, REDIRECT_URI, new MockHttpServletRequest(), new MockHttpServletResponse()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH_PROVIDER_ERROR);
    }

    @Test
    @DisplayName("로그인 - provider 조회 실패 시 OAuth2HttpClient에서 던진 예외가 전파된다")
    void loginUnknownProvider() {
        given(oauth2HttpClient.fetchUserInfo("unknown", CODE, REDIRECT_URI))
                .willThrow(new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 provider: unknown"));

        assertThatThrownBy(() -> service.login("unknown", CODE, REDIRECT_URI, new MockHttpServletRequest(), new MockHttpServletResponse()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    // ── connect() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("계정 연동 - 성공 시 linkSocialAccount가 올바른 인자로 호출된다")
    void connectSuccess() {
        given(oauth2HttpClient.fetchUserInfo(PROVIDER, CODE, REDIRECT_URI)).willReturn(kakaoAttrs);

        service.connect(USER_ID, PROVIDER, CODE, REDIRECT_URI);

        verify(userService).linkSocialAccount(eq(USER_ID), eq("KAKAO"), eq(EMAIL), eq(PROVIDER_ID));
    }

    @Test
    @DisplayName("계정 연동 - 이미 연동된 계정이면 BusinessException이 전파된다")
    void connectDuplicateAccountThrowsException() {
        given(oauth2HttpClient.fetchUserInfo(PROVIDER, CODE, REDIRECT_URI)).willReturn(kakaoAttrs);
        willThrow(new BusinessException(ErrorCode.DUPLICATED_ADDRESS, "이미 연동된 소셜 계정입니다."))
                .given(userService)
                .linkSocialAccount(any(), anyString(), anyString(), anyString());

        assertThatThrownBy(() -> service.connect(USER_ID, PROVIDER, CODE, REDIRECT_URI))
                .isInstanceOf(BusinessException.class);
    }
}
