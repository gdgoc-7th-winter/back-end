package com.project.algo.application.dto;

import com.project.algo.domain.entity.AnswerComment;
import com.project.algo.domain.enums.CommentTag;

import java.time.Instant;
import java.util.List;

public record AnswerCommentResponse(
        Long commentId,
        Long authorId,
        String authorNickname,
        String content,
        List<Integer> referencedLines,
        CommentTag commentTag,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {
    public static AnswerCommentResponse from(AnswerComment comment) {
        return new AnswerCommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getContent(),
                List.copyOf(comment.getReferencedLines()),
                comment.getCommentTag(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
