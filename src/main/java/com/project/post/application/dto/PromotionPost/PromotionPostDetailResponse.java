package com.project.post.application.dto.PromotionPost;

import com.project.post.application.dto.PostDetailResponse;
import com.project.post.domain.enums.PromotionCategory;

public record PromotionPostDetailResponse(
        PromotionCategory category,
        PostDetailResponse post
) {
}
