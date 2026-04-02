package com.project.post.domain.repository;

import com.project.post.domain.repository.dto.AppliedRecruitingPostListQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationSubmissionRepositoryCustom {

    Page<AppliedRecruitingPostListQueryResult> findAppliedRecruitingPostListByUserId(
            Long userId,
            Pageable pageable
    );
}