package com.project.user.application.service.impl;

import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.entity.UserContribution;
import com.project.contribution.domain.repository.ContributionScoreRepository;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;

import com.project.user.application.dto.UserSession;
import com.project.global.event.Impl.UserPromotionEvent;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.EmailAuthRepository;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.domain.repository.UserRepository;
import com.project.user.presentation.dto.request.LoginRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;
import com.project.user.presentation.dto.request.SignUpRequest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final LevelBadgeRepository levelBadgeRepository;
    private final UserContributionRepository userContributionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final ContributionScoreRepository contributionScoreRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // 회원가입
    @Override
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
        try {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            User user = new User(request.getEmail(), encodedPassword);
            userRepository.save(user);
            LevelBadge initialBadge = levelBadgeRepository.findByPointWithinRange(user.getTotalPoint())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
            user.initializeLevelBadge(initialBadge);
            emailAuthRepository.deleteRegisterSession(request.getEmail());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATED_ADDRESS);
        }
    }

    @Override
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

    @Override
    @Transactional
    public void completeInitialProfile(Long id, ProfileUpdateRequest request, HttpSession session) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 사용자입니다."));

        user.updateProfile(
                request.getNickname(), request.getStudentId(), request.getDepartment(), request.getTrack(),
                request.getProfilePicture(), request.getTechStacks(), request.getInterests()
        );
        user.grantUserAuthority();
        eventPublisher.publishEvent(new UserPromotionEvent(user.getId(), user.getId()));
    }

    @Override
    @Transactional
    public void updateSecurityContext(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                id, null, List.of(new SimpleGrantedAuthority(user.getAuthority().name())));
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "회원 정보가 없습니다."));
        return ProfileResponse.from(user);
    }

    @Override
    @Transactional
    public User earnAScore(Long id, String scoreName, Long referenceId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "회원 정보가 없습니다."));
        ContributionScore contributionScore = contributionScoreRepository.findByName(scoreName)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        user.updatePoint(contributionScore.getPoint());

        try {
            UserContribution contribution = UserContribution.builder()
                    .user(user)
                    .contributionScore(contributionScore)
                    .referenceId(referenceId)
                    .build();
            userContributionRepository.save(contribution);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATED_ADDRESS);
        }
        return user;
    }
}
