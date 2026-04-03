package com.project.post.application.dto;

import java.time.Instant;
import java.util.List;

public record PostListResponse(
        Long postId,
        String title,
        String contentPreview,
        String thumbnailUrl,
        PostAuthorResponse author,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        PostViewerResponse viewer,
        List<String> tagNames,
        Instant createdAt
) {
}
