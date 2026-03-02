package com.project.global.resolver;

import com.project.global.annotation.CurrentUser;
import com.project.user.application.service.UserSessionService;
import com.project.user.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * -----------------------------------------------------------------------
 * `@CurrentUser`가 붙은 User 파라미터를 세션에서 조회해 주입하는 ArgumentResolver
 * -----------------------------------------------------------------------
 */
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserSessionService userSessionService;

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpSession session = request != null ? request.getSession(false) : null;
        return userSessionService.getCurrentUser(session);
    }
}
