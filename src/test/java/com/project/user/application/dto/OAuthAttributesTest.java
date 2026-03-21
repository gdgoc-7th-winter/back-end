package com.project.user.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthAttributesTest {

    @Test
    @DisplayName("Google 속성에서 providerId(sub)와 email이 올바르게 추출된다")
    void ofGoogle() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("sub", "google-provider-id");
        attrs.put("email", "user@google.com");

        OAuthAttributes result = OAuthAttributes.of("google", "sub", attrs);

        assertThat(result.providerId()).isEqualTo("google-provider-id");
        assertThat(result.email()).isEqualTo("user@google.com");
        assertThat(result.nameAttributeKey()).isEqualTo("sub");
    }

    @Test
    @DisplayName("Kakao 속성에서 providerId(id)와 kakao_account 이메일이 올바르게 추출된다")
    void ofKakao() {
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "user@kakao.com");

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("id", 12345L);
        attrs.put("kakao_account", kakaoAccount);

        OAuthAttributes result = OAuthAttributes.of("kakao", "id", attrs);

        assertThat(result.providerId()).isEqualTo("12345");
        assertThat(result.email()).isEqualTo("user@kakao.com");
    }

    @Test
    @DisplayName("Naver 속성에서 response 내부의 id와 email이 올바르게 추출된다")
    void ofNaver() {
        Map<String, Object> response = new HashMap<>();
        response.put("id", "naver-provider-id");
        response.put("email", "user@naver.com");

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("response", response);

        OAuthAttributes result = OAuthAttributes.of("naver", "response", attrs);

        assertThat(result.providerId()).isEqualTo("naver-provider-id");
        assertThat(result.email()).isEqualTo("user@naver.com");
    }

    @Test
    @DisplayName("GitHub 속성에서 providerId(id)와 email이 올바르게 추출된다")
    void ofGithub() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("id", 99999);
        attrs.put("email", "user@github.com");

        OAuthAttributes result = OAuthAttributes.of("github", "id", attrs);

        assertThat(result.providerId()).isEqualTo("99999");
        assertThat(result.email()).isEqualTo("user@github.com");
    }

    @Test
    @DisplayName("알 수 없는 provider는 Google 방식으로 처리된다")
    void ofUnknownFallsBackToGoogle() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("sub", "unknown-provider-id");
        attrs.put("email", "user@unknown.com");

        OAuthAttributes result = OAuthAttributes.of("unknown", "sub", attrs);

        assertThat(result.providerId()).isEqualTo("unknown-provider-id");
        assertThat(result.email()).isEqualTo("user@unknown.com");
    }
}
