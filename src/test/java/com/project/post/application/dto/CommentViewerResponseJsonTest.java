package com.project.post.application.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentViewerResponseJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("JSON 필드명은 liked, isAuthor 이다 (스크랩 없음)")
    void serializesViewerFieldsForFrontendContract() throws Exception {
        String json = objectMapper.writeValueAsString(new CommentViewerResponse(true, true));

        assertThat(json).contains("\"liked\":true");
        assertThat(json).contains("\"isAuthor\":true");
        assertThat(json).doesNotContain("scrapped");
    }
}
