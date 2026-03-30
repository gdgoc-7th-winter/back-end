package com.project.user.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.user.application.service.impl.UserSessionService;
import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;
import com.project.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSessionService userSessionService;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    // ── getCurrentUser ────────────────────────────────────────────────────────

    @Test
    @DisplayName("세션이 null이면 SESSION_EXPIRED를 던진다")
    void getCurrentUserThrowsWhenSessionNull() {
        assertThatThrownBy(() -> userSessionService.getCurrentUser(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SESSION_EXPIRED);

        verify(userRepository, never()).findActiveById(any());
    }

    @Test
    @DisplayName("세션에 LOGIN_USER가 없으면 SESSION_EXPIRED를 던진다")
    void getCurrentUserThrowsWhenNoLoginUser() {
        // LOGIN_USER 없는 세션 — deleteUser 후 동일 탭에서 재요청 시 서버가 새 빈 세션을 생성하는 경우와 동일
        assertThatThrownBy(() -> userSessionService.getCurrentUser(session))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SESSION_EXPIRED);

        verify(userRepository, never()).findActiveById(any());
    }

    /**
     * 시나리오 3-A (다른 탭): 탈퇴 처리 후 다른 탭/세션의 요청.
     *
     * LOGIN_USER 세션 속성은 살아있지만, findActiveById가 empty를 반환한다.
     * 이 경우 RESOURCE_NOT_FOUND를 던져 요청을 거부해야 한다.
     */
    @Test
    @DisplayName("탈퇴 후 다른 탭 요청: findActiveById가 empty이면 RESOURCE_NOT_FOUND를 던진다")
    void getCurrentUserThrowsWhenUserWithdrawn() {
        Long userId = 1L;
        session.setAttribute("LOGIN_USER", activeSession(userId));
        given(userRepository.findActiveById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userSessionService.getCurrentUser(session))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("활성 유저는 getCurrentUser에서 정상 반환된다")
    void getCurrentUserReturnsActiveUser() {
        Long userId = 1L;
        User user = buildUser(userId, "nick");

        session.setAttribute("LOGIN_USER", activeSession(userId));
        given(userRepository.findActiveById(userId)).willReturn(Optional.of(user));

        User result = userSessionService.getCurrentUser(session);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getNickname()).isEqualTo("nick");
    }

    // ── findOptionalUser ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findOptionalUser: 세션이 null이면 Optional.empty()를 반환한다")
    void findOptionalUserReturnsEmptyWhenSessionNull() {
        Optional<User> result = userSessionService.findOptionalUser(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findOptionalUser: LOGIN_USER가 없으면 Optional.empty()를 반환한다")
    void findOptionalUserReturnsEmptyWhenNoLoginUser() {
        Optional<User> result = userSessionService.findOptionalUser(session);
        assertThat(result).isEmpty();
    }

    /**
     * 시나리오 3-B (다른 탭): 선택적 인증 엔드포인트에서 탈퇴 사용자 요청.
     *
     * findOptionalUser는 인증 여부와 무관하게 동작하는 엔드포인트에서 사용된다.
     * 탈퇴 후 findActiveById가 empty를 반환하면 Optional.empty()로 처리해야 한다.
     */
    @Test
    @DisplayName("탈퇴 후 다른 탭 요청: findOptionalUser는 Optional.empty()를 반환한다")
    void findOptionalUserReturnsEmptyWhenUserWithdrawn() {
        Long userId = 1L;
        session.setAttribute("LOGIN_USER", activeSession(userId));
        given(userRepository.findActiveById(userId)).willReturn(Optional.empty());

        Optional<User> result = userSessionService.findOptionalUser(session);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findOptionalUser: 활성 유저이면 Optional에 User를 담아 반환한다")
    void findOptionalUserReturnsUserWhenActive() {
        Long userId = 1L;
        User user = buildUser(userId, "nick");

        session.setAttribute("LOGIN_USER", activeSession(userId));
        given(userRepository.findActiveById(userId)).willReturn(Optional.of(user));

        Optional<User> result = userSessionService.findOptionalUser(session);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
    }

    // ── 시나리오 3-C (동일 세션) ──────────────────────────────────────────────

    /**
     * 시나리오 3-C (동일 세션): deleteUser 후 동일 탭 후속 요청.
     *
     * deleteUser → logout → session.invalidate().
     * MockHttpSession은 invalidate() 이후 getAttribute()에서 IllegalStateException을 던진다.
     * 실제 서블릿 컨테이너에서는 무효화된 세션 ID로 들어온 요청에 새 빈 세션을 생성하므로
     * LOGIN_USER가 없는 상태가 되어 SESSION_EXPIRED로 처리된다.
     *
     * 여기서는 "빈 새 세션" 상태를 MockHttpSession으로 시뮬레이션한다.
     */
    @Nested
    @DisplayName("탈퇴 직후 동일 세션 시나리오")
    class SameSessionAfterWithdrawal {

        @Test
        @DisplayName("탈퇴로 세션 무효화 후 서버가 생성한 새 빈 세션에서는 SESSION_EXPIRED를 반환한다")
        void getCurrentUserThrowsOnNewEmptySessionAfterWithdrawal() {
            // 실제 흐름: session.invalidate() → 브라우저 쿠키 만료 → 다음 요청에 새 빈 세션 생성
            MockHttpSession newEmptySession = new MockHttpSession(); // LOGIN_USER 없음

            assertThatThrownBy(() -> userSessionService.getCurrentUser(newEmptySession))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.SESSION_EXPIRED);
        }

        @Test
        @DisplayName("탈퇴로 세션 무효화 후 findOptionalUser는 empty를 반환한다")
        void findOptionalUserReturnsEmptyOnNewEmptySessionAfterWithdrawal() {
            MockHttpSession newEmptySession = new MockHttpSession();

            Optional<User> result = userSessionService.findOptionalUser(newEmptySession);

            assertThat(result).isEmpty();
        }
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private static UserSession activeSession(Long userId) {
        return UserSession.builder()
                .userId(userId)
                .authority(Authority.USER)
                .needsProfile(false)
                .build();
    }

    private static User buildUser(Long id, String nickname) {
        User user = User.builder().email("test@test.com").password("pw").nickname(nickname).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
