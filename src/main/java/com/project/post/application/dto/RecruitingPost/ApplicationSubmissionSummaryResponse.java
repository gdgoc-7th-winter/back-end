package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.entity.ApplicationSubmission;

import java.time.Instant;

public record ApplicationSubmissionSummaryResponse(
        Long submissionId,
        String applicantName,
        String campus,
        String department,
        Instant submittedAt
) {
    public static ApplicationSubmissionSummaryResponse from(ApplicationSubmission submission) {
        return new ApplicationSubmissionSummaryResponse(
                submission.getId(),
                submission.getApplicantName(),
                submission.getCampus().name(),
                submission.getDepartment().getName(),
                submission.getSubmittedAt()
        );
    }
}
