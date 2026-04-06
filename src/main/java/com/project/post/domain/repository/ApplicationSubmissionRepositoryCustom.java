package com.project.post.domain.repository;

import com.project.post.domain.enums.RecruitingStatus;
import com.project.post.domain.repository.dto.AppliedRecruitingPostListQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.time.Instant;

public interface ApplicationSubmissionRepositoryCustom {

    Page<AppliedRecruitingPostListQueryResult> findAppliedRecruitingPostListByUserId(
            Long userId,
            @Nullable RecruitingStatus status,
            Instant now,
            Pageable pageable
    );
}
