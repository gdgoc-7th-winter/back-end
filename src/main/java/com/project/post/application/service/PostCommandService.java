package com.project.post.application.service;

import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PostUpdateRequest;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;
public interface PostCommandService {

    Long create(@NonNull String boardCode, @NonNull PostCreateRequest request, @NonNull User author);

    void update(@NonNull Long postId, @NonNull PostUpdateRequest request, @NonNull User author);

    void softDelete(@NonNull Long postId, @NonNull User author);

    void increaseViewCount(@NonNull Long postId);
}
