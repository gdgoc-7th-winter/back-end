package com.project.post.domain.repository;

import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PostRepositoryCustom {

    Page<PostListQueryResult> findPostList(@NonNull String boardCode, @NonNull Pageable pageable);

    Optional<PostDetailQueryResult> findPostDetail(@NonNull Long postId);
}
