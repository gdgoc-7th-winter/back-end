package com.project.post.application.service;

import com.project.post.application.dto.PostCommentRequest;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;
public interface PostCommentCommandService {

    Long create(@NonNull Long postId, @NonNull PostCommentRequest request, @NonNull User user);

    void softDelete(@NonNull Long postId, @NonNull Long commentId, @NonNull User user);
}
