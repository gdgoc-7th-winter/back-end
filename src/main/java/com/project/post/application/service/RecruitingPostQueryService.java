package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.MyRecruitingPostListResponse;
import com.project.post.application.dto.RecruitingPost.RecruitingPostDetailResponse;

public interface RecruitingPostQueryService {

    RecruitingPostDetailResponse getDetail(Long postId, Long userId);

    MyRecruitingPostListResponse getMyRecruitingPosts(Long userId);
}