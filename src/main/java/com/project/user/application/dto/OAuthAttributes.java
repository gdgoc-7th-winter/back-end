package com.project.user.application.dto;

import java.util.Map;

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
        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) attributes.get("sub"), // 구글은 'sub'가 유니크 ID
                (String) attributes.get("email")
        );
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                String.valueOf(attributes.get("id")), // 카카오는 최상위 'id'
                (String) kakaoAccount.get("email")
        );
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) response.get("id"), // 네이버는 response 안의 'id'
                (String) response.get("email")
        );
    }

    private static OAuthAttributes ofGithub(String userNameAttributeName, Map<String, Object> attributes) {
        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                String.valueOf(attributes.get("id")),
                (String) attributes.get("login")
        );
    }
}
