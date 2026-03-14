package com.project.post.application.service;

import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostListResponse;
import com.project.post.domain.enums.PromotionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

public interface PromotionPostQueryService {
    PromotionPostDetailResponse getDetail(@NonNull Long postId);
    Page<PromotionPostListResponse> getList(
            PromotionCategory category,
            Pageable pageable
    );
}
