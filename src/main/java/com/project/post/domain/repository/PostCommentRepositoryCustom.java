package com.project.post.domain.repository;

import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.dto.ReplyPreviewRow;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;

/**
 * 댓글은 게시글과 달리 @SQLRestriction을 쓰지 않는다.
 * 삭제 댓글도 목록에 포함하고, 서비스에서 deleted/content 마스킹 처리.
 */
public interface PostCommentRepositoryCustom {

    List<PostComment> findRootCommentsWithCursor(
            @NonNull Long postId,
            @Nullable Instant cursorCreatedAt,
            @Nullable Long cursorId,
            int limitPlusOne);

    List<ReplyPreviewRow> findReplyPreviewRows(
            @NonNull Long postId,
            @NonNull List<Long> parentCommentIds,
            int limitPlusOne);

    List<PostComment> findCommentsByParentWithCursor(
            @NonNull Long postId,
            @NonNull Long parentCommentId,
            @Nullable Instant cursorCreatedAt,
            @Nullable Long cursorId,
            int limitPlusOne);
}
