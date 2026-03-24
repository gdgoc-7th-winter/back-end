package com.project.global.resolver;

import com.project.global.annotation.OptionalSessionUser;
import com.project.user.application.service.impl.UserSessionService;
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OptionalSessionUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserSessionService userSessionService;

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(OptionalSessionUser.class)) {
            return false;
        }
        if (!Optional.class.equals(parameter.getParameterType())) {
            return false;
        }
        Type generic = parameter.getGenericParameterType();
        if (!(generic instanceof ParameterizedType pt)) {
            return false;
        }
        return pt.getActualTypeArguments()[0].equals(User.class);
    }

    @Override
    @NonNull
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return Optional.<User>empty();
        }
        HttpSession session = request.getSession(false);
        return userSessionService.findOptionalUser(session);
    }
}
