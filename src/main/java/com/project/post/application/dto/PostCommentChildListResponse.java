package com.project.post.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.project.global.response.CursorPageResponse;

import java.util.List;

@JsonPropertyOrder({"comments", "nextCursor", "hasNext"})
public record PostCommentChildListResponse(
        List<PostCommentResponse> comments,
        String nextCursor,
        boolean hasNext
) {
    public static PostCommentChildListResponse from(CursorPageResponse<PostCommentResponse> page) {
        return new PostCommentChildListResponse(page.items(), page.nextCursor(), page.hasNext());
    }
}
