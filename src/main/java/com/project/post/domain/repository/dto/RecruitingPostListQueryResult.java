package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.ApplicationType;
import com.project.post.domain.enums.RecruitingCategory;

import java.time.Instant;

public record RecruitingPostListQueryResult(
        RecruitingCategory category,
        ApplicationType applicationType,
        Instant startedAt,
        Instant deadlineAt,

        Long postId,
        String title,
        String thumbnailUrl,

        Long authorId,
        String authorNickname,
        String authorProfileImgUrl,
        String authorDepartmentName,
        String authorRepresentativeTrackName,
        String authorLevelImageUrl,
        boolean authorIsWithdrawn,

        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,

        Instant createdAt
) {
}
