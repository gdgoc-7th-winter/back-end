package com.project.post.application.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;

import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

public final class PostCommentCursorCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private PostCommentCursorCodec() {
    }

    public static String encode(Instant createdAt, Long id) {
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(id, "id");
        try {
            Payload payload = new Payload(createdAt, id);
            byte[] json = MAPPER.writeValueAsBytes(payload);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            throw new IllegalStateException("커서 인코딩 실패", e);
        }
    }

    public static Cursor decode(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(raw.trim());
            Payload payload = MAPPER.readValue(decoded, Payload.class);
            return new Cursor(payload.createdAt, payload.id);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "유효하지 않은 커서입니다.");
        }
    }

    public record Cursor(Instant createdAt, Long id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Payload(Instant createdAt, Long id) {
    }
}
