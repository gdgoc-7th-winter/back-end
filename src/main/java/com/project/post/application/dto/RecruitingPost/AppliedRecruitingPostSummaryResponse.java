package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.enums.RecruitingStatus;

import java.time.Instant;

public record AppliedRecruitingPostSummaryResponse(
        Long submissionId,
        Long postId,
        String title,
        RecruitingCategory category,
        RecruitingStatus status,
        Instant startedAt,
        Instant deadlineAt,
        Instant submittedAt
) {
    public static AppliedRecruitingPostSummaryResponse from(ApplicationSubmission submission) {
        RecruitingPost recruitingPost = submission.getRecruitingApplication().getRecruitingPost();
        Post post = recruitingPost.getPost();

        return new AppliedRecruitingPostSummaryResponse(
                submission.getId(),
                recruitingPost.getId(),
                post.getTitle(),
                recruitingPost.getCategory(),
                recruitingPost.getStatus(),
                recruitingPost.getStartedAt(),
                recruitingPost.getDeadlineAt(),
                submission.getSubmittedAt()
        );
    }
}