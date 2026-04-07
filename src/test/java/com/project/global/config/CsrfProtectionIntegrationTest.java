package com.project.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CSRF가 걸린 POST에 대해: 토큰 없음 → 403, Spring Security 테스트용 {@code csrf()} 포함 → CSRF 통과 후 인증 단계로 진행.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CsrfProtectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CSRF 보호 POST: X-XSRF-TOKEN 없으면 403")
    void postWithoutCsrfReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/view"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF 보호 POST: csrf()로 토큰을 넣으면 403이 아니고, 미로그인이면 401")
    void postWithCsrfPassesCsrfThenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/view")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
