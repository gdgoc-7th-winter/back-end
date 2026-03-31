package com.project.post.application.service;

import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface PostQueryService {

    Page<PostListResponse> getList(
            @NonNull String boardCode,
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            String order,
            @Nullable Long viewerUserId);

    Page<PostListResponse> getListAllBoards(
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            String order,
            @Nullable Long viewerUserId);

    PostDetailResponse getDetail(@NonNull Long postId, @Nullable Long viewerUserId);
}
