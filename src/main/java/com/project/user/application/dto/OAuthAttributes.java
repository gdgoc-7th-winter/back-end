package com.project.user.application.dto;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;

import java.util.Map;
import java.util.Optional;

public record OAuthAttributes(
        Map<String, Object> attributes,
        String nameAttributeKey,
        String providerId,
        String email
) {
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        } else if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        } else if ("github".equals(registrationId)) {
            return ofGithub(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        String providerId = Optional.ofNullable((String) attributes.get("sub"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Google 응답에서 사용자 ID를 찾을 수 없습니다."));
        String email = Optional.ofNullable((String) attributes.get("email"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Google 응답에서 이메일을 찾을 수 없습니다."));
        return new OAuthAttributes(attributes, userNameAttributeName, providerId, email);
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = Optional.ofNullable((Map<String, Object>) attributes.get("kakao_account"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Kakao 응답에서 계정 정보를 찾을 수 없습니다."));
        String providerId = Optional.ofNullable(attributes.get("id"))
                .map(String::valueOf)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Kakao 응답에서 사용자 ID를 찾을 수 없습니다."));
        String email = Optional.ofNullable((String) kakaoAccount.get("email"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Kakao 응답에서 이메일을 찾을 수 없습니다."));
        return new OAuthAttributes(attributes, userNameAttributeName, providerId, email);
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = Optional.ofNullable((Map<String, Object>) attributes.get("response"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Naver 응답에서 사용자 정보를 찾을 수 없습니다."));
        String providerId = Optional.ofNullable((String) response.get("id"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Naver 응답에서 사용자 ID를 찾을 수 없습니다."));
        String email = Optional.ofNullable((String) response.get("email"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Naver 응답에서 이메일을 찾을 수 없습니다."));
        return new OAuthAttributes(attributes, userNameAttributeName, providerId, email);
    }

    private static OAuthAttributes ofGithub(String userNameAttributeName, Map<String, Object> attributes) {
        String providerId = Optional.ofNullable(attributes.get("id"))
                .map(String::valueOf)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "GitHub 응답에서 사용자 ID를 찾을 수 없습니다."));
        String login = Optional.ofNullable((String) attributes.get("login"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "GitHub 응답에서 사용자 아이디를 찾을 수 없습니다."));
        return new OAuthAttributes(attributes, userNameAttributeName, providerId, login);
    }
}
