package com.project.post.application.service;

import com.project.post.application.dto.PostCommentChildListResponse;
import com.project.post.application.dto.PostCommentRootListResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PostCommentQueryService {

    PostCommentRootListResponse getComments(
            @NonNull Long postId,
            @Nullable String cursor,
            int size,
            @Nullable Long viewerUserId);

    PostCommentChildListResponse getChildComments(
            @NonNull Long postId,
            @NonNull Long parentCommentId,
            @Nullable String cursor,
            int size,
            @Nullable Long viewerUserId);
}
