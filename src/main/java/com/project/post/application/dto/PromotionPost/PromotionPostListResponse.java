package com.project.post.application.dto.PromotionPost;

import com.project.post.application.dto.PostListResponse;
import com.project.post.domain.enums.PromotionCategory;

public record PromotionPostListResponse(
        PromotionCategory category,
        PostListResponse post
) {
}
