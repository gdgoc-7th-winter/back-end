package com.project.post.application.dto.RecruitingPost;

import java.util.List;

public record ApplicationSubmissionListResponse(
        List<ApplicationSubmissionSummaryResponse> submissions
) {
}