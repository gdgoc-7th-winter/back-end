package com.project.post.application.dto;

import java.time.Instant;
import java.util.List;

public record PostCommentResponse(
        Long commentId,
        Long postId,
        Long userId,
        String userNickname,
        boolean isWithdrawn,
        Long parentCommentId,
        int depth,
        String content,
        boolean isDeleted,
        long likeCount,
        Instant createdAt,
        CommentViewerResponse viewer,
        List<PostCommentResponse> replies,
        boolean hasMoreReplies
) {
}
