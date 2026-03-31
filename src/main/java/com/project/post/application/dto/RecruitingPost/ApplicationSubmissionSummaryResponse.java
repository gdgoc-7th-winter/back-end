package com.project.post.application.dto.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
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
        if (submission.getCampus() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "지원서 요약 매핑 중 campus 값이 누락되었습니다. submissionId=" + submission.getId()
            );
        }

        if (submission.getDepartment() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "지원서 요약 매핑 중 department 값이 누락되었습니다. submissionId=" + submission.getId()
            );
        }

        return new ApplicationSubmissionSummaryResponse(
                submission.getId(),
                submission.getApplicantName(),
                submission.getCampus().name(),
                submission.getDepartment().getName(),
                submission.getSubmittedAt()
        );
    }
}
