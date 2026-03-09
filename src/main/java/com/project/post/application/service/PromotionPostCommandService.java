package com.project.post.application.service;

import com.project.post.application.dto.PromotionPost.PromotionPostCreateRequest;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;

public interface PromotionPostCommandService {

    Long create(@NonNull PromotionPostCreateRequest request, @NonNull User author);
}