package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.ApplicationFormDetailResponse;

public interface ApplicationFormQueryService {
    ApplicationFormDetailResponse getApplicationFormDetail(Long recruitingPostId);
}