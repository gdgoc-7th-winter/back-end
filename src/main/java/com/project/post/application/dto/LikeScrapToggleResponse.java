package com.project.post.application.dto;

public record LikeScrapToggleResponse(
        boolean liked,
        long count
) {
}
