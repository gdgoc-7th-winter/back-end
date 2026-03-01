package com.project.post.application.dto;

import java.time.Instant;

public record PostListResponse(
        Long postId,
        String title,
        String thumbnailUrl,
        String authorNickname,
        long viewCount,
        long likeCount,
        long commentCount,
        Instant createdAt
) {
}
