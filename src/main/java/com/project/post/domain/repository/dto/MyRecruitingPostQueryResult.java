package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.RecruitingCategory;

import java.time.Instant;

public record MyRecruitingPostQueryResult(
        Long recruitingPostId,
        String title,
        String contentPreview,
        String thumbnailUrl,
        String authorNickname,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Instant createdAt,
        RecruitingCategory category,
        Instant startedAt,
        Instant deadlineAt
) {
}
