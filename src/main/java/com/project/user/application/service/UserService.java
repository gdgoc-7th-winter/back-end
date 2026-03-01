package com.project.user.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;

import com.project.user.application.dto.UserSession;
import com.project.user.application.dto.request.UserPromotionEvent;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.EmailAuthRepository;
import com.project.user.domain.repository.UserRepository;
import com.project.user.presentation.dto.request.LoginRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;
import com.project.user.presentation.dto.request.SignUpRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    // 회원가입
    @Transactional
    public void signUp(SignUpRequest request) {
        // Redis에서 이메일 인증 완료 여부 최종 확인
        if (!emailAuthRepository.hasRegisterSession(request.getEmail())) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        // 이메일 중복 체크 (DB 조회)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATED_ADDRESS);
        }

        // 비밀번호 암호화 및 엔티티 생성
        try{
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            User user = new User(request.getEmail(), encodedPassword);
            userRepository.save(user);
        } catch (DataIntegrityViolationException e){
            throw new BusinessException(ErrorCode.DUPLICATED_ADDRESS);
        } finally {
            emailAuthRepository.deleteRegisterSession(request.getEmail());
        }
    }

    @Transactional
    public void login(LoginRequest request, HttpSession session, HttpServletRequest servletRequest) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String role = user.getAuthority().name();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, authorities);

        CsrfToken csrfToken = (CsrfToken) servletRequest.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);

        servletRequest.changeSessionId();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        UserSession userSession = UserSession.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .authority(user.getAuthority())
                .needsProfile(user.needsInitialSetup())
                .build();

        session.setAttribute("LOGIN_USER", userSession);

    }

    @Transactional
    public void completeInitialProfile(String email, ProfileUpdateRequest request, HttpSession session) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 사용자입니다."));

        user.updateProfile(
                request.getNickname(), request.getStudentId(), request.getDepartment(), request.getTrack(),
                request.getProfilePicture(), request.getTechStacks(), request.getInterests()
        );
        eventPublisher.publishEvent(new UserPromotionEvent(user.getEmail(), session, user.getId()));
    }

    @Transactional
    public void updateSecurityContext(String email) {
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "회원 정보가 없습니다."));
        return ProfileResponse.from(user);
    }
}
