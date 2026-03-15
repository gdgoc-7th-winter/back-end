package com.project.global.interceptor;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.domain.enums.Authority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class OnboardingInterceptorTest {

    @InjectMocks
    private OnboardingInterceptor onboardingInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new Object(); // 더미 핸들러
    }

    @Test
    @DisplayName("DUMMY 권한 유저가 일반 API 접근 시 ACCESS_DENIED 예외를 던진다")
    void preHandleBlockDummyUser() throws Exception {
        // given
        UserSession dummySession = UserSession.builder()
                .userId(1L)
                .email("dummy@hufs.ac.kr")
                .authority(Authority.DUMMY)
                .needsProfile(true)
                .build();

        request.setRequestURI("/api/users/me");
        request.getSession().setAttribute("LOGIN_USER", dummySession);

        // when & then
        assertThatThrownBy(() -> onboardingInterceptor.preHandle(request, response, handler))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("DUMMY 권한 유저라도 프로필 설정 API 접근은 허용됨")
    void preHandleAllowProfileSetup() throws Exception {
        // given
        UserSession dummySession = UserSession.builder()
                .userId(1L)
                .email("dummy@hufs.ac.kr")
                .authority(Authority.DUMMY)
                .needsProfile(true)
                .build();

        request.setRequestURI("/api/users/profile-setup");
        request.getSession().setAttribute("LOGIN_USER", dummySession);

        // when
        boolean result = onboardingInterceptor.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("프로필 설정이 완료된(USER) 유저는 모든 API 접근이 허용됨")
    void preHandleAllowNormalUser() throws Exception {
        // given
        UserSession userSession = UserSession.builder()
                .userId(1L)
                .email("user@hufs.ac.kr")
                .authority(Authority.USER)
                .needsProfile(false)
                .build();

        request.setRequestURI("/api/users/me");
        request.getSession().setAttribute("LOGIN_USER", userSession);

        // when
        boolean result = onboardingInterceptor.preHandle(request, response, handler);

        // then
        assertThat(result).isTrue();
    }
}
