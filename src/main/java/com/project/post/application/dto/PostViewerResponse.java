package com.project.post.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostViewerResponse(
        boolean liked,
        boolean scrapped,
        @JsonProperty("isAuthor")
        boolean author
) {
    private static final PostViewerResponse GUEST = new PostViewerResponse(false, false, false);

    public static PostViewerResponse guest() {
        return GUEST;
    }
}
