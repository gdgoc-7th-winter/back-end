package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.RecruitingPostCreateRequest;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;

public interface RecruitingPostCommandService {
    Long create(@NonNull RecruitingPostCreateRequest request, @NonNull User user);
}