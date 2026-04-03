package com.project.post.domain.repository.dto;

import java.time.Instant;

public record PostListQueryResult(
        Long postId,
        String title,
        String contentPreview,
        String thumbnailUrl,
        Long authorId,
        String authorNickname,
        String authorProfileImgUrl,
        String authorDepartmentName,
        String authorRepresentativeTrackName,
        String authorTierBadgeImageUrl,
        boolean authorWithdrawn,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        Instant createdAt
) {
}
