package com.project.post.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

public record PostListResponse(
        Long postId,
        String title,
        String thumbnailUrl,
        String authorNickname,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        @Schema(description = "게시글 태그 목록")
        List<String> tagNames,
        Instant createdAt
) {
}
