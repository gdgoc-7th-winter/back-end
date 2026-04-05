package com.project.post.application.dto.RecruitingPost;

import com.project.post.application.dto.PostListResponse;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.enums.RecruitingStatus;

import java.time.Instant;

public record AppliedRecruitingPostSummaryResponse(
        Long submissionId,
        RecruitingCategory category,
        RecruitingStatus status,
        String statusLabel,
        Instant startedAt,
        Instant deadlineAt,
        Instant submittedAt,
        PostListResponse post
) {
}
