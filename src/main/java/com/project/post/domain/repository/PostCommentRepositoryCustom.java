package com.project.post.domain.repository;

import com.project.post.domain.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 댓글은 게시글과 달리 @SQLRestriction을 쓰지 않는다.
 * 삭제 댓글도 목록에 포함하고, 서비스에서 deleted/content 마스킹 처리.
 */
public interface PostCommentRepositoryCustom {

    /** 루트 댓글 (삭제 포함, 마스킹은 서비스에서) */
    Page<PostComment> findRootComments(@NonNull Long postId, @NonNull Pageable pageable);

    List<PostComment> findRepliesByParentId(@NonNull Long parentId, int limit);

    /** 대댓글 (삭제 포함, 마스킹은 서비스에서) */
    List<PostComment> findRepliesByParentIds(@NonNull List<Long> parentIds);
}
