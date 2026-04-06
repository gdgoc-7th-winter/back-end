package com.project.global.config;

import com.project.user.domain.repository.impl.CustomAuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CsrfCookieProperties csrfCookieProperties;
    private final OAuth2ConnectSuccessHandler oAuth2ConnectSuccessHandler;
    private final CsrfAwareAccessDeniedHandler csrfAwareAccessDeniedHandler;

    @Bean
    public CsrfSetCookieLoggingFilter csrfSetCookieLoggingFilter() {
        return new CsrfSetCookieLoggingFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomAuthorizationRequestRepository customAuthorizationRequestRepository() {
        return new CustomAuthorizationRequestRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http,
                                                   final CsrfSetCookieLoggingFilter csrfSetCookieLoggingFilter) throws Exception {
        http.addFilterBefore(csrfSetCookieLoggingFilter, CsrfFilter.class);
        return http
                .sessionManagement(session -> session.sessionFixation().changeSessionId())
                .csrf(csrf -> {
                    CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
                    csrfRepo.setCookiePath(csrfCookieProperties.path());
                    csrfRepo.setCookieCustomizer(builder -> {
                        String cookieDomain = csrfCookieProperties.domainForSetCookie();
                        if (cookieDomain != null) {
                            builder.domain(cookieDomain);
                        }
                        builder.path(csrfCookieProperties.path());
                        builder.secure(csrfCookieProperties.secure());
                        builder.sameSite(csrfCookieProperties.sameSite());
                    });
                    csrf.csrfTokenRepository(csrfRepo)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(
                                "/actuator/health", "/actuator/health/**", "/actuator/info",
                                "/api/health", "/api/ping", "/api/v1/auth/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api/v1/users/signup", "/api/v1/users/login",
                                "/login/oauth2/code/**"
                        );
                })
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 개발/테스트 전용 — ADMIN 전용으로 제한 (prod에서는 @Profile("!prod")로 Bean 자체가 없음)
                        .requestMatchers("/api/v1/dev/**").hasRole("ADMIN")
                        // 사용자/개발자 확인용
                        .requestMatchers("/api/health", "/api/ping").permitAll()
                        // ALB 헬스 체크
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/users/signup", "/api/v1/users/login", "/api/v1/users/logout").permitAll()
                        // OAuth2 로그인 시작 (비로그인 사용자도 접근 가능)
                        .requestMatchers(HttpMethod.GET, "/api/v1/oauth2/login/**").permitAll()
                        // 정적 리소스 (테스트 UI)
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/css/**", "/js/**").permitAll()
                        // 비로그인 허용: 학과 목록 조회
                        .requestMatchers(HttpMethod.GET, "/api/v1/departments").permitAll()
                        // 비로그인 허용: 게시글 목록·댓글 목록 등 (게시글 본문 상세는 인증 필요)
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/boards/*/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/promotions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/promotions/{postId}").permitAll()
                        // 그 외 모든 요청은 인증 필요 (Redis 세션 기반 인증 적용 예정)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler(csrfAwareAccessDeniedHandler)
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestRepository(customAuthorizationRequestRepository())
                        )
                        .successHandler(oAuth2ConnectSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 Login Failed: {}", exception.getMessage());
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth2 인증 실패");
                        })
                )
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }
}
