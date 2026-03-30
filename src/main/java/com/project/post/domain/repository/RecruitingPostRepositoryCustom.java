package com.project.post.domain.repository;

import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.repository.dto.RecruitingPostListQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface RecruitingPostRepositoryCustom {

    Page<RecruitingPostListQueryResult> findRecruitingPostList(
            @Nullable RecruitingCategory category,
            @NonNull Pageable pageable
    );
}