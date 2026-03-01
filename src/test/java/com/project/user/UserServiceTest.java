package com.project.user;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;
import com.project.user.domain.repository.EmailAuthRepository;
import com.project.user.domain.repository.UserRepository;
import com.project.user.presentation.dto.request.LoginRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailAuthRepository emailAuthRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private UserService userService;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("로그인 성공 시 세션에 유저 정보와 보안 컨텍스트가 저장되어야 한다")
    void loginSuccess() {
        // given
        String email = "test@hufs.ac.kr";
        String password = "password123";
        User user = new User(email, "encodedPassword");
        LoginRequest request = new LoginRequest(email, password);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

        // when
        userService.login(request, session);

        // then
        // 세션에 LOGIN_USER가 저장되었는지 확인
        UserSession userSession = (UserSession) session.getAttribute("LOGIN_USER");

        assertThat(userSession).isNotNull();
        assertThat(userSession.getEmail()).isEqualTo(email);
        assertThat(userSession.isNeedsProfile()).isTrue();

        // 세션에 Spring Security Context가 저장되었는지 확인
        Object securityContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(securityContext).isInstanceOf(SecurityContext.class);

        Authentication auth = ((SecurityContext) securityContext).getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo(email);
        assertThat(auth.getAuthorities()).extracting("authority").contains("DUMMY");
    }


    @Test
    @DisplayName("초기 프로필 설정 완료 시 권한이 USER로 승격되고 세션이 갱신되어야 한다")
    void completeInitialProfileSuccess() {
        // given
        String email = "test@hufs.ac.kr";
        User user = spy(new User(email, "password")); // updateProfile 감시를 위해 spy 사용
        ProfileUpdateRequest request = createProfileUpdateRequest(); // 테스트용 DTO 생성 메서드

        // 기존 세션 설정
        UserSession oldSession = UserSession.builder()
                .userId(1L).email(email).authority(Authority.DUMMY).needsProfile(true).build();
        session.setAttribute("LOGIN_USER", oldSession);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        userService.completeInitialProfile(email, request, session);

        // then
        // 1. 엔티티 상태 변경 확인
        verify(user).promoteToUser();

        // 2. 갱신된 세션 확인
        UserSession updatedSession = (UserSession) session.getAttribute("LOGIN_USER");
        assertThat(updatedSession.getAuthority()).isEqualTo(Authority.USER);
        assertThat(updatedSession.isNeedsProfile()).isFalse();

        // 3. SecurityContext 갱신 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities()).extracting("authority").contains("USER");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 BusinessException이 발생한다")
    void loginFailUserNotFound() {
        // given
        LoginRequest request = new LoginRequest("none@hufs.ac.kr", "pwd");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request, session))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    private ProfileUpdateRequest createProfileUpdateRequest() {
        return new ProfileUpdateRequest("닉네임", "202001234", "컴퓨터공학", "BACKEND", null, null, null);
    }
}
