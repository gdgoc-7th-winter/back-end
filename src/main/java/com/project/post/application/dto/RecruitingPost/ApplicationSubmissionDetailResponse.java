package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.enums.Campus;

import java.time.Instant;
import java.util.List;

public record ApplicationSubmissionDetailResponse(
        Long submissionId,
        Long recruitingPostId,
        String applicantName,
        Campus campus,
        String department,
        Instant submittedAt,
        List<ApplicationSubmissionAnswerResponse> answers
) {
}
