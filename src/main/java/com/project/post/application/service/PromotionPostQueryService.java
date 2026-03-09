package com.project.post.application.service;

import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import io.micrometer.common.lang.NonNull;

public interface PromotionPostQueryService {
    PromotionPostDetailResponse getDetail(@NonNull Long postId);
}
