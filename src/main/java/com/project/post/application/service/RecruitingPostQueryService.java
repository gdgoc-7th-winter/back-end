package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.MyRecruitingPostListResponse;
import com.project.post.application.dto.RecruitingPost.RecruitingPostDetailResponse;
import com.project.post.application.dto.RecruitingPost.RecruitingPostListResponse;
import com.project.post.domain.enums.RecruitingCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public interface RecruitingPostQueryService {

    RecruitingPostDetailResponse getDetail(Long postId, Long userId);

    Page<RecruitingPostListResponse> getList(
            @Nullable RecruitingCategory category,
            Pageable pageable,
            @Nullable Long viewerUserId
    );

    MyRecruitingPostListResponse getMyRecruitingPosts(Long userId);
}
