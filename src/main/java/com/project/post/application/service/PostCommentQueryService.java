package com.project.post.application.service;

import com.project.post.application.dto.PostCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PostCommentQueryService {

    Page<PostCommentResponse> getComments(
            @NonNull Long postId,
            @NonNull Pageable pageable,
            @Nullable Long viewerUserId);
}
