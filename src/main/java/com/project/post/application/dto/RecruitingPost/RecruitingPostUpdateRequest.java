package com.project.post.application.dto.RecruitingPost;

import com.project.post.application.dto.PostUpdateRequest;
import com.project.post.domain.enums.RecruitingCategory;
import jakarta.validation.Valid;

import java.time.Instant;

public record RecruitingPostUpdateRequest(

        RecruitingCategory category,

        Instant startedAt,

        Instant deadlineAt,

        @Valid
        PostUpdateRequest post
) {
}
