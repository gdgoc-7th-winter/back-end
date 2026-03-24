package com.project.post.application.dto.RecruitingPost;

import java.time.Instant;
import java.util.List;

public record ApplicationSubmissionDetailResponse(
        Long submissionId,
        Long recruitingPostId,
        Instant submittedAt,
        List<ApplicationSubmissionAnswerResponse> answers
) {
}