package com.project.post.application.dto.RecruitingPost;

import com.project.post.application.dto.PostListResponse;
import com.project.post.domain.enums.ApplicationType;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.enums.RecruitingStatus;

import java.time.Instant;

public record RecruitingPostListResponse(
        RecruitingCategory category,
        ApplicationType applicationType,
        RecruitingStatus status,
        Instant startedAt,
        Instant deadlineAt,
        PostListResponse post
) {
}