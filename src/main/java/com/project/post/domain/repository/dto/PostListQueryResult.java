package com.project.post.domain.repository.dto;

import java.time.Instant;

public record PostListQueryResult(
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
