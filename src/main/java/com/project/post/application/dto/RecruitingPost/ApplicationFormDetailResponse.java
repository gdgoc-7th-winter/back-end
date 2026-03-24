package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.enums.ApplicationType;

import java.time.Instant;
import java.util.List;

public record ApplicationFormDetailResponse(
        Long recruitingPostId,
        Long recruitingApplicationId,
        ApplicationType applicationType,
        Instant startedAt,
        Instant deadlineAt,
        String title,
        String message,
        List<ApplicationFormQuestionResponse> questions
) {
    public static ApplicationFormDetailResponse of(
            RecruitingPost recruitingPost,
            RecruitingApplication recruitingApplication,
            List<ApplicationFormQuestionResponse> questions
    ) {
        return new ApplicationFormDetailResponse(
                recruitingPost.getId(),
                recruitingApplication.getId(),
                recruitingPost.getApplicationType(),
                recruitingPost.getStartedAt(),
                recruitingPost.getDeadlineAt(),
                recruitingApplication.getTitle(),
                recruitingApplication.getMessage(),
                questions
        );
    }
}