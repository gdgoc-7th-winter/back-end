package com.project.user.application.service.oauth;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.OAuthAttributes;
import com.project.user.application.dto.UserSession;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SocialLoginServiceImpl implements SocialLoginService {

    private final OAuth2HttpClient oauth2HttpClient;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Override
    public void login(String provider, String code, String redirectUri, HttpServletRequest request, HttpServletResponse response) {
        OAuthAttributes attrs = oauth2HttpClient.fetchUserInfo(provider, code, redirectUri);

        User user = userRepository.findByProviderAndProviderId(provider.toUpperCase(), attrs.providerId())
                .orElseThrow(() -> {
                    log.warn("연동된 계정이 없음 - provider: {}, providerId: {}", provider, attrs.providerId());
                    return new BusinessException(ErrorCode.OAUTH_PROVIDER_ERROR, "연동된 계정이 없습니다.");
                });

        createSession(user, request, response);
        log.info("소셜 로그인 완료 - provider: {}, userId: {}", provider, user.getId());
    }

    @Override
    public void connect(Long userId, String provider, String code, String redirectUri) {
        OAuthAttributes attrs = oauth2HttpClient.fetchUserInfo(provider, code, redirectUri);
        userService.linkSocialAccount(userId, provider.toUpperCase(), attrs.email(), attrs.providerId());
        log.info("소셜 계정 연동 완료 - provider: {}, userId: {}", provider, userId);
    }

    private void createSession(User user, HttpServletRequest request, HttpServletResponse response) {
        request.getSession(true);
        request.changeSessionId();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority(user.getAuthority().name())));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        UserSession userSession = UserSession.builder()
                .userId(user.getId())
                .authority(user.getAuthority())
                .needsProfile(user.needsInitialSetup())
                .build();

        request.getSession().setAttribute("LOGIN_USER", userSession);
        log.info("세션 생성 완료: {}", user.getEmail());
    }
}
