package com.project.post.domain.repository;

import com.project.post.domain.enums.PromotionCategory;
import com.project.post.domain.repository.dto.PromotionPostListQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PromotionPostRepositoryCustom {

    Page<PromotionPostListQueryResult> findPromotionPostList(
            @Nullable PromotionCategory category,
            @NonNull Pageable pageable);
}
