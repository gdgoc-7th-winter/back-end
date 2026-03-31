package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.RecruitingCategory;

import java.time.Instant;

public record AppliedRecruitingPostListQueryResult(
        Long submissionId,

        RecruitingCategory category,
        Instant startedAt,
        Instant deadlineAt,
        Instant submittedAt,

        Long postId,
        String title,
        String thumbnailUrl,

        Long authorId,
        String authorNickname,
        String authorProfileImgUrl,
        String authorDepartmentName,
        String authorRepresentativeTrackName,
        String authorLevelImageUrl,

        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,

        Instant createdAt
) {
}