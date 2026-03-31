package com.project.post.domain.repository;

import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.dto.ReplyPreviewRow;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;

/**
 * 댓글 커스텀 조회.
 * 삭제된 댓글도 함께 조회할 수 있으며, 응답 마스킹은 애플리케이션 계층에서 처리한다.
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
