package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.RecruitingPostDetailResponse;

public interface RecruitingPostQueryService {

    RecruitingPostDetailResponse getDetail(Long postId, Long userId);
}