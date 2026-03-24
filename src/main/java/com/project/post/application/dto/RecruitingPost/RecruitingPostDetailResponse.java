package com.project.post.application.dto.RecruitingPost;

import com.project.post.application.dto.PostDetailResponse;
import com.project.post.domain.enums.ApplicationType;
import com.project.post.domain.enums.RecruitingCategory;

import java.time.Instant;

public record RecruitingPostDetailResponse(

        RecruitingCategory category,
        ApplicationType applicationType,

        Instant startedAt,
        Instant deadlineAt,

        PostDetailResponse post

) {
}