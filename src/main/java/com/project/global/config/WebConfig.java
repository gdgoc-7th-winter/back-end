package com.project.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.project.global.interceptor.OnboardingInterceptor;
import com.project.global.resolver.CurrentUserArgumentResolver;

import java.util.List;

@Configuration
public class
WebConfig implements WebMvcConfigurer {
    private final @NonNull OnboardingInterceptor onboardingInterceptor;
    private final @NonNull CurrentUserArgumentResolver currentUserArgumentResolver;

    public WebConfig(@NonNull OnboardingInterceptor onboardingInterceptor,
                     @NonNull CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.onboardingInterceptor = onboardingInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(onboardingInterceptor)
                .addPathPatterns("/api/**")

                .excludePathPatterns(
                        "/api/health",
                        "/api/v1/auth/**",
                        "/api/v1/users/login",
                        "/api/v1/users/signup",
                        "/api/v1/users/logout",
                        "/api/v1/oauth2/login/**",
                        "/api/v1/departments"
                );
    }

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
