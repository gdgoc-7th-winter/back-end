package com.project.post.application.dto.PromotionPost;

import com.project.post.application.dto.PostCreateRequest;
import com.project.post.domain.enums.PromotionCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PromotionPostCreateRequest(

        @NotNull(message = "카테고리는 필수입니다.")
        PromotionCategory category,

        @NotNull(message = "게시글 정보는 필수입니다.")
        @Valid
        PostCreateRequest post
) {
}