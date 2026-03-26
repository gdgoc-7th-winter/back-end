package com.project.post.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommentViewerResponse(
        boolean liked,
        @JsonProperty("isAuthor")
        boolean author
) {
    private static final CommentViewerResponse GUEST = new CommentViewerResponse(false, false);

    public static CommentViewerResponse guest() {
        return GUEST;
    }
}
