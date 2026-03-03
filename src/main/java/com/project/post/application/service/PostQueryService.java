package com.project.post.application.service;

import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface PostQueryService {

    Page<PostListResponse> getList(
            @NonNull String boardCode,
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            String order);

    PostDetailResponse getDetail(@NonNull Long postId);
}
