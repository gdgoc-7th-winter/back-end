package com.project.post.domain.repository;

import com.project.post.domain.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;

public interface PostCommentRepositoryCustom {

    Page<PostComment> findRootComments(@NonNull Long postId, @NonNull Pageable pageable);

    List<PostComment> findRepliesByParentId(@NonNull Long parentId, int limit);

    List<PostComment> findRepliesByParentIds(@NonNull List<Long> parentIds);
}
