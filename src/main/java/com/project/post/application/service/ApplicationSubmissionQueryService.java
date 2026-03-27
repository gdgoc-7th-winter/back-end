package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionDetailResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionListResponse;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;

public interface ApplicationSubmissionQueryService {
    ApplicationSubmissionDetailResponse getDetail(@NonNull Long submissionId,
                                                  @NonNull User user);
    ApplicationSubmissionListResponse getSubmissionList(Long postId, User user);
}