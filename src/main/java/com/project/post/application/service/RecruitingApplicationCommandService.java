package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.SubmitApplicationRequest;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionUpdateRequest;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;

public interface RecruitingApplicationCommandService {

    void submit(@NonNull SubmitApplicationRequest request,
                @NonNull User user);

    void updateSubmission(@NonNull Long submissionId,
                          @NonNull ApplicationSubmissionUpdateRequest request,
                          @NonNull User user);
}