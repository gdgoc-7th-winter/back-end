package com.project.user.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.event.impl.UserPromotionEvent;

import com.project.user.application.dto.UserSession;
import com.project.user.application.service.impl.UserServiceImpl;
import com.project.user.domain.entity.Department;
import com.project.user.domain.entity.TechStack;
import com.project.user.domain.entity.Track;
import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;

import com.project.user.domain.repository.DepartmentRepository;
import com.project.user.domain.repository.TechStackRepository;
import com.project.user.domain.repository.TrackRepository;
import com.project.user.domain.repository.UserRepository;

import com.project.user.presentation.dto.request.PasswordUpdateRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private TrackRepository trackRepository;
    @Mock private TechStackRepository techStackRepository;
    @Mock private DepartmentRepository departmentRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        session = new MockHttpSession();
        request.setSession(session);
    }

    @Test
    @DisplayName("로그인 성공 시 세션에 유저 정보가 저장되어야 한다")
    void loginSuccess() {
        try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
            // given
            mockedContext.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));

            String email = "test@hufs.ac.kr";
            String password = "password123";
            User user = User.builder().email(email).password("encodedPassword").nickname("testuser").build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

            // when
            userService.login(email, password);

            // then
            UserSession userSession = (UserSession) session.getAttribute("LOGIN_USER");
            assertThat(userSession).isNotNull();
            assertThat(userSession.getAuthority()).isEqualTo(Authority.DUMMY);
        }
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치 시 예외 발생")
    void loginFailInvalidPassword() {
        try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
            // given
            mockedContext.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));

            User user = User.builder().email("test@hufs.ac.kr").password("encoded").nickname("nick").build();
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login("test@hufs.ac.kr", "wrong"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);
        }
    }

    @Test
    @DisplayName("프로필 초기 설정 시 권한이 승급되고 이벤트가 발행된다")
    void updateProfilePromotionSuccess() {
        try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
            // given
            mockedContext.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));

            Long userId = 1L;
            User user = User.builder().email("test@hufs.ac.kr").password("pw").nickname("nick").build();
            ReflectionTestUtils.setField(user, "id", userId);

            Department mockDept = Department.builder().college("공과대학").name("컴퓨터공학").build();
            Track mockTrack = Track.builder().name("백엔드").build();
            TechStack mockTech = TechStack.builder().name("Spring").build();

            ProfileUpdateRequest updateRequest = ProfileUpdateRequest.builder()
                    .nickname("newNick")
                    .studentId("202001234")
                    .departmentId(1L)
                    .trackNames(List.of("백엔드"))
                    .techStackNames(List.of("Spring"))
                    .build();

            given(userRepository.findActiveById(userId)).willReturn(Optional.of(user));
            given(departmentRepository.findById(1L)).willReturn(Optional.of(mockDept));
            given(trackRepository.findByNameIn(anyList())).willReturn(List.of(mockTrack));
            given(techStackRepository.findByNameIn(anyList())).willReturn(List.of(mockTech));

            // when
            userService.updateProfile(userId, updateRequest);

            // then
            assertThat(user.getAuthority()).isEqualTo(Authority.USER);
            assertThat(user.needsInitialSetup()).isFalse();
            verify(eventPublisher, times(1)).publishEvent(any(UserPromotionEvent.class));
        }
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePasswordSuccess() {
        // given
        Long userId = 1L;
        String encodedCurrent = "encoded_currentPw";
        String encodedNew = "encoded_newPw123!";
        User user = User.builder().email("test@hufs.ac.kr").password(encodedCurrent).nickname("nick").build();
        ReflectionTestUtils.setField(user, "id", userId);

        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest();
        ReflectionTestUtils.setField(passwordUpdateRequest, "oldPassword", "currentPw");
        ReflectionTestUtils.setField(passwordUpdateRequest, "newPassword", "newPw123!");

        given(userRepository.findActiveById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("currentPw", encodedCurrent)).willReturn(true);
        given(passwordEncoder.encode("newPw123!")).willReturn(encodedNew);

        // when
        userService.changePassword(userId, passwordUpdateRequest);

        // then
        verify(passwordEncoder).matches("currentPw", encodedCurrent);
        verify(passwordEncoder).encode("newPw123!");
        assertThat(user.getPassword()).isEqualTo(encodedNew);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치 시 예외 발생")
    void changePasswordFailMismatch() {
        // given
        Long userId = 1L;
        String encodedCurrent = "encoded_correctPw";
        User user = User.builder().email("test@hufs.ac.kr").password(encodedCurrent).nickname("nick").build();
        ReflectionTestUtils.setField(user, "id", userId);

        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest();
        ReflectionTestUtils.setField(passwordUpdateRequest, "oldPassword", "wrongPw");
        ReflectionTestUtils.setField(passwordUpdateRequest, "newPassword", "newPw123!");

        given(userRepository.findActiveById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPw", encodedCurrent)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.changePassword(userId, passwordUpdateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - soft delete 처리되고 세션이 무효화된다")
    void deleteUserSoftDeletesUser() {
        try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
            // given
            mockedContext.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request, response));

            Long userId = 1L;
            User user = User.builder().email("test@hufs.ac.kr").password("pw").nickname("nick").build();
            ReflectionTestUtils.setField(user, "id", userId);

            given(userRepository.findActiveById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.encode(eq(User.WITHDRAWN_PASSWORD_PLACEHOLDER))).willReturn("encoded-withdrawn");

            // when
            userService.deleteUser(userId);

            // then
            assertThat(user.isDeleted()).isTrue();
            verify(userRepository, never()).delete(any());
            assertThat(session.isInvalid()).isTrue();
        }
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 개인정보가 마스킹된다")
    void deleteUserMasksPersonalInfo() {
        try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
            // given
            mockedContext.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request, response));

            Long userId = 1L;
            User user = User.builder().email("test@hufs.ac.kr").password("encodedPassword").nickname("홍길동").build();
            ReflectionTestUtils.setField(user, "id", userId);
            ReflectionTestUtils.setField(user, "studentId", "202001234");
            ReflectionTestUtils.setField(user, "profileImgUrl", "https://s3.example.com/profile.jpg");
            ReflectionTestUtils.setField(user, "introduction", "안녕하세요");

            given(userRepository.findActiveById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.encode(eq(User.WITHDRAWN_PASSWORD_PLACEHOLDER))).willReturn("encoded-withdrawn");

            // when
            userService.deleteUser(userId);

            // then
            assertThat(user.getEmail()).isEqualTo("deleted_" + userId + "@deleted.invalid");
            assertThat(user.getNickname()).isEqualTo(null);
            assertThat(user.getPassword()).isEqualTo("encoded-withdrawn");
            assertThat(user.getStudentId()).isNull();
            assertThat(user.getProfileImgUrl()).isNull();
            assertThat(user.getIntroduction()).isNull();
        }
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 유저")
    void deleteUserFailsWhenUserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findActiveById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);

        verify(userRepository, never()).delete(any());
    }
}
