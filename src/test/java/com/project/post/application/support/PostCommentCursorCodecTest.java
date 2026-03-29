package com.project.post.application.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostCommentCursorCodecTest {

    @Test
    @DisplayName("커서 인코딩·디코딩이 동일 (createdAt 나노초 포함)")
    void roundTrip() {
        Instant t = Instant.parse("2026-03-29T12:00:00.123456789Z");
        String encoded = PostCommentCursorCodec.encode(t, 42L);
        PostCommentCursorCodec.Cursor c = PostCommentCursorCodec.decode(encoded);
        assertThat(c.createdAt()).isEqualTo(t);
        assertThat(c.id()).isEqualTo(42L);
    }
}
