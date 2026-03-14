package com.project.post.application.dto.PromotionPost;

import com.project.post.application.dto.PostUpdateRequest;
import com.project.post.domain.enums.PromotionCategory;
import jakarta.validation.Valid;

public record PromotionPostUpdateRequest(
        PromotionCategory category,
        @Valid PostUpdateRequest post
) {
}
