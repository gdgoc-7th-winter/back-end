package com.project.user.application.service.oauth;

import com.project.global.error.BusinessException;
import com.project.user.application.dto.OAuthAttributes;
import com.project.user.application.dto.UserSession;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final HttpSession session;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 1. 서비스 이름 (google, kakao, user)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 2. 각 서비스의 유니크 키 (google="sub", kakao="id" 등)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 3. 통합 추출 객체 생성
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        String providerId = attributes.providerId();
        String email = attributes.email();

        Long linkUserId = (Long) session.getAttribute("LINK_USER_ID");

        if (linkUserId == null) {
            return userRepository.findByProviderAndProviderId(registrationId.toUpperCase(), providerId)
                    .map(user -> {
                        log.info("연동된 유저 발견: {}. 로그인을 진행합니다.", user.getEmail());
                        oauthLogin(user);

                        return new DefaultOAuth2User(
                                List.of(new SimpleGrantedAuthority(user.getAuthority().name())),
                                oAuth2User.getAttributes(),
                                userNameAttributeName
                        );
                    }
                    )
                    .orElseThrow(() -> {
                        log.warn("연동된 계정이 없습니다.");
                        return new OAuth2AuthenticationException("연동된 계정이 없습니다.");
                    });
        } else {
            try {
                userService.linkSocialAccount(linkUserId, registrationId.toUpperCase(), email, providerId);
            } catch (BusinessException e) {
                throw new OAuth2AuthenticationException(e.getMessage());
            }
            session.removeAttribute("LINK_USER_ID");
            session.setAttribute("OAUTH2_OPERATION", "link");
        }
        return oAuth2User;
    }

    private void oauthLogin(User user) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = servletRequest.getSession(true);

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
                .authority(user.getAuthority())
                .needsProfile(user.needsInitialSetup())
                .build();

        session.setAttribute("LOGIN_USER", userSession);
        log.info("세션 생성 완료: {}", user.getEmail());
    }
}
