package com.project.global.config;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(
                                "/actuator/health", "/actuator/health/**", "/actuator/info",
                                "/api/health", "/api/ping","/api/auth/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api/users/signup", "/api/users/login", "/api/users/logout"
                        )
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/users/login")
                        .successHandler((request, response, authentication) -> {
                            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                            if (csrfToken != null) {
                                response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
                            }

                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"success\": true, \"message\": \"로그인 성공\"}");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/api/users/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");

                            try{
                                response.setStatus(HttpServletResponse.SC_OK);
                                String jsonResponse = "{\"success\": true, \"data\": \"로그아웃 되었습니다.\", \"message\": null}";
                                response.getWriter().write(jsonResponse);
                            } catch (IOException e) {
                                log.error("Logout success response writing failed", e);
                                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부에 문제가 있습니다. 관리팀에 문의하세요.");
                            }
                        })
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 사용자/개발자 확인용
                        .requestMatchers("/api/health", "/api/ping").permitAll()
                        // ALB 헬스 체크
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 이메일 인증용
                        .requestMatchers("/api/auth/**").permitAll()

                        // 회원가입 및 로그인용
                        .requestMatchers("/api/users/signup").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/users/logout").permitAll()

                        // 그 외 모든 요청은 인증 필요 (Redis 세션 기반 인증 적용 예정)
                        .anyRequest().authenticated()
                )
                .build();
    }
}
