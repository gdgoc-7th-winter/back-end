package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionUpdateRequest;
import com.project.post.application.dto.RecruitingPost.SubmitApplicationRequest;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;

public interface RecruitingApplicationCommandService {

    Long submit(@NonNull Long postId,
                @NonNull SubmitApplicationRequest request,
                @NonNull User user);

    void updateSubmission(@NonNull Long submissionId,
                          @NonNull ApplicationSubmissionUpdateRequest request,
                          @NonNull User user);

    void cancelSubmission(@NonNull Long submissionId,
                          @NonNull User user);
}
