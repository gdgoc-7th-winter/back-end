package com.project.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.project.global.interceptor.OnboardingInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final OnboardingInterceptor onboardingInterceptor;

    public WebConfig(OnboardingInterceptor onboardingInterceptor) {
        this.onboardingInterceptor = onboardingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(onboardingInterceptor)
                .addPathPatterns("/api/**")

                .excludePathPatterns(
                        "/api/health",
                        "/api/auth/**",
                        "/api/users/login",
                        "/api/users/signup",
                        "/api/users/profile-setup"
                );
    }
}
