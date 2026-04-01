package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionSummaryResponse;
import com.project.post.application.dto.RecruitingPost.AppliedRecruitingPostSummaryResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionDetailResponse;
import com.project.post.domain.enums.Campus;
import com.project.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

public interface ApplicationSubmissionQueryService {

    ApplicationSubmissionDetailResponse getDetail(@NonNull Long submissionId,
                                                  @NonNull User user);

    Page<ApplicationSubmissionSummaryResponse> getSubmissionList(
            @NonNull Long postId,
            @NonNull User user,
            Campus campus,
            Long departmentId,
            String applicantName,
            String sort,
            @NonNull Pageable pageable
    );

    Page<AppliedRecruitingPostSummaryResponse> getAppliedRecruitings(
            @NonNull User user,
            @NonNull Pageable pageable
    );
}
