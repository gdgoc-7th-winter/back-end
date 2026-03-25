package com.project.post.application.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostViewerResponseJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("JSON 필드명은 liked, scrapped, isAuthor 이다")
    void serializesViewerFieldsForFrontendContract() throws Exception {
        String json = objectMapper.writeValueAsString(new PostViewerResponse(true, false, true));

        assertThat(json).contains("\"liked\":true");
        assertThat(json).contains("\"scrapped\":false");
        assertThat(json).contains("\"isAuthor\":true");
    }
}
