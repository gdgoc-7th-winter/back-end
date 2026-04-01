package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.enums.RecruitingStatus;

import java.time.Instant;
import java.util.List;

public record ApplicationSubmissionListResponse(
        Long postId,
        String title,
        RecruitingCategory category,
        RecruitingStatus status,
        String statusLabel,
        Instant startedAt,
        Instant deadlineAt,

        long totalElements,
        int totalPages,
        int size,
        List<ApplicationSubmissionSummaryResponse> content,
        int number,
        int numberOfElements,
        boolean first,
        boolean last,
        boolean empty
) {
}
