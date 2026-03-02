package com.project.post.application.service;

import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface PostQueryService {

    Page<PostListResponse> getList(@NonNull String boardCode, @NonNull Pageable pageable);

    PostDetailResponse getDetail(@NonNull Long postId);
}
