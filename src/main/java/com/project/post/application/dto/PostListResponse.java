package com.project.post.application.dto;

import java.time.Instant;
import java.util.List;

public record PostListResponse(
        Long postId,
        String title,
        String thumbnailUrl,
        PostAuthorResponse author,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        List<String> tagNames,
        Instant createdAt
) {
}
