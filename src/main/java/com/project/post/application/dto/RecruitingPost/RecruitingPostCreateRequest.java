package com.project.post.application.dto.RecruitingPost;

import com.project.post.application.dto.PostCreateRequest;
import com.project.post.domain.enums.ApplicationType;
import com.project.post.domain.enums.RecruitingCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RecruitingPostCreateRequest(

        @NotNull(message = "카테고리는 필수입니다.")
        RecruitingCategory category,

        @NotNull(message = "지원 방식은 필수입니다.")
        ApplicationType applicationType,

        Instant startedAt,
        Instant deadlineAt,

        @Valid
        ApplicationFormRequest applicationForm,

        @NotNull(message = "게시글 정보는 필수입니다.")
        @Valid
        PostCreateRequest post
) {
    @AssertTrue(message = "지원 방식이 FORM인 경우 applicationForm은 필수입니다.")
    public boolean isValidApplicationForm() {
        return applicationType != ApplicationType.FORM || applicationForm != null;
    }
}
