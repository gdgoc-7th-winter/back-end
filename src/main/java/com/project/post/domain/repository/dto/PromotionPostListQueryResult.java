package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.PromotionCategory;

import java.time.Instant;

public record PromotionPostListQueryResult(
        PromotionCategory category,
        Long postId,
        String title,
        String thumbnailUrl,
        Long authorId,
        String authorNickname,
        String authorProfileImgUrl,
        String authorDepartmentName,
        String authorRepresentativeTrackName,
        String authorTierBadgeImageUrl,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        Instant createdAt
) {
}
