package com.project.user.application.service.impl;

import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.entity.UserContribution;
import com.project.contribution.domain.repository.ContributionScoreRepository;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;

import com.project.user.application.dto.UserSession;
import com.project.global.event.Impl.UserPromotionEvent;
import com.project.user.application.dto.request.UserRegistrationCompletedEvent;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.application.service.UserService;

import com.project.user.domain.entity.Department;
import com.project.user.domain.entity.User;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.TechStack;
import com.project.user.domain.entity.Track;
import com.project.user.domain.entity.SocialAccount;

import com.project.user.domain.repository.DepartmentRepository;
import com.project.user.domain.repository.EmailAuthRepository;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.domain.repository.TrackRepository;
import com.project.user.domain.repository.UserRepository;
import com.project.user.domain.repository.TechStackRepository;

import com.project.user.presentation.dto.request.PasswordUpdateRequest;
import com.project.user.presentation.dto.request.ProfilePatchRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;
import com.project.user.presentation.dto.request.SignUpRequest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final LevelBadgeRepository levelBadgeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final TechStackRepository techStackRepository;
    private final TrackRepository trackRepository;
    private final DepartmentRepository departmentRepository;

    private final ContributionScoreRepository contributionScoreRepository;
    private final UserContributionRepository userContributionRepository;

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
            String email = request.getEmail();
            String nickname = request.getNickname();
            User user = new User(email, encodedPassword, nickname);
            userRepository.save(user);
            LevelBadge initialBadge = levelBadgeRepository.findByPointWithinRange(user.getTotalPoint())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

            user.initializeLevelBadge(initialBadge);
            eventPublisher.publishEvent(new UserRegistrationCompletedEvent(request.getEmail()));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "회원가입 실행 중 오류가 발생했습니다. 다시 시도해주세요");
        }
    }

    @Override
    @Transactional
    public void login(String email, String password) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = servletRequest.getSession(true);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String role = user.getAuthority().name();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                email, null, authorities);

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
                .authority(user.getAuthority())
                .needsProfile(user.needsInitialSetup())
                .build();

        session.setAttribute("LOGIN_USER", userSession);
    }

    @Override
    @Transactional
    public void logout(HttpSession session) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        if (session != null) {
            session.invalidate();
        }

        // 3. SecurityContext 초기화 (인증 정보 삭제)
        SecurityContextHolder.clearContext();

        // 4. JSESSIONID 쿠키 삭제를 위해 클라이언트에 전달
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!user.needsInitialSetup()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 초기 프로필 설정이 완료된 계정입니다.");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 학과입니다."));
        List<Track> trackMasters = trackRepository.findByNameIn(request.getTrackNames());
        List<TechStack> techStackMasters = techStackRepository.findByNameIn(request.getTechStackNames());

        user.updateProfile(
                request.getNickname(),
                request.getStudentId(),
                department,
                request.getProfilePicture(),
                request.getIntroduction(),
                trackMasters,
                techStackMasters
        );

        if (!user.needsInitialSetup()) {
            user.grantUserAuthority();
            updateSecurityContext(user.getId());
            eventPublisher.publishEvent(new UserPromotionEvent(user.getId(), user.getId()));
            log.info("유저 승급 이벤트 형성");
        }
    }

    @Override
    @Transactional
    public void patchProfile(Long userId, ProfilePatchRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (user.needsInitialSetup()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "초기 프로필 설정을 먼저 완료해주세요.");
        }

        validateNotBlank(request.getNickname(), "닉네임");
        validateNotBlank(request.getStudentId(), "학번");

        Department department = request.getDepartmentId() != null
                ? departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 학과입니다."))
                : null;
        List<Track> trackMasters = request.getTrackNames() != null
                ? trackRepository.findByNameIn(request.getTrackNames()) : null;
        List<TechStack> techStackMasters = request.getTechStackNames() != null
                ? techStackRepository.findByNameIn(request.getTechStackNames()) : null;

        user.updateProfile(
                request.getNickname(),
                request.getStudentId(),
                department,
                request.getProfilePicture(),
                request.getIntroduction(),
                trackMasters,
                techStackMasters
        );
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value != null && value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "은(는) 공백일 수 없습니다.");
        }
    }

    @Override
    @Transactional
    public void updateSecurityContext(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        HttpServletRequest request =  ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String authorityName = user.getAuthority().name();
        if (!authorityName.startsWith("ROLE_")) {
            authorityName = "ROLE_" + authorityName;
        }

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authorityName));

        // 2. 새로운 Authentication 객체 생성 (기존 Principal 타입과 일치시켜야 함)
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), // 로그 로그보니 이메일 형식이네요! 202003824@hufs.ac.kr
                null,
                authorities
        );

        // 3. 현재 스레드의 SecurityContext 갱신
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(newAuth);

        // 🚨 4. 중요: HttpSession에 변경된 SecurityContext를 명시적으로 저장
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            log.info("🌐 세션에 새로운 SecurityContext 저장 완료: {}", authorityName);
        }
    }

    @Override
    @Transactional
    public void changePassword(Long id, PasswordUpdateRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        String currentPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        if (!currentPassword.equals(user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        } else {
            user.changePassword(newPassword);
        }
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
            log.error("Unexpected error during score initialization for: {}", contributionScore.getName() + e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT, "작업 실행 중 오류가 발생했습니다. 관리팀에 문의주세요.");
        }
        return user;
    }

    @Override
    @Transactional
    public void linkSocialAccount(Long userId, String provider, String email, String providerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        boolean alreadyLinked = user.getSocialAccounts().stream()
                .anyMatch(acc -> acc.getProvider().equals(provider));

        if (alreadyLinked) {
            throw new BusinessException(ErrorCode.DUPLICATED_ADDRESS, "이미 등록하신 소셜 로그인 게정입니다.");
        }

        if (userRepository.existsBySocialAuth(email,provider)) {
            throw new BusinessException(ErrorCode.DUPLICATED_ADDRESS, "이미 사용되고 있는 계정 정보입니다.");
        }

        user.addSocialAccount(new SocialAccount(provider, email, providerId));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        userRepository.delete(user);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        logout(request.getSession(false));
    }
}
