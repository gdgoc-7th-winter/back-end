package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.enums.RecruitingStatus;
import com.project.post.domain.enums.RecruitingCategory;

import java.time.Instant;

public record MyRecruitingPostSummaryResponse(
        Long postId,
        String title,
        String contentPreview,
        String thumbnailUrl,
        String authorNickname,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Instant createdAt,
        RecruitingStatus status,
        String statusLabel,
        RecruitingCategory category,
        Instant startedAt,
        Instant deadlineAt
) {
}
