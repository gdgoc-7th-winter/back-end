package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.Campus;

import java.time.Instant;

public record LecturePostListQueryResult(
        Long postId,
        String title,
        String thumbnailUrl,
        Long authorId,
        String authorNickname,
        String authorProfileImgUrl,
        String authorDepartmentName,
        String authorRepresentativeTrackName,
        String authorTierBadgeImageUrl,
        boolean authorWithdrawn,
        String department,
        Campus campus,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        Instant createdAt
) {
}
