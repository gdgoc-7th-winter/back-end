package com.project.post.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"comments", "nextCursor", "hasNext", "totalCommentCount"})
public record PostCommentRootListResponse(
        List<PostCommentResponse> comments,
        String nextCursor,
        boolean hasNext,
        long totalCommentCount
) {
}
