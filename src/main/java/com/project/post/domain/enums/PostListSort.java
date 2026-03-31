package com.project.post.domain.enums;

public enum PostListSort {
    LATEST,
    VIEWS,
    LIKES,
    //좋아요 수 + 조회수 합으로 정렬 (인기)
    POPULAR;

    public static PostListSort from(String value) {
        if (value == null || value.isBlank()) {
            return LATEST;
        }
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "views", "view" -> VIEWS;
            case "likes", "like" -> LIKES;
            case "popular", "hot" -> POPULAR;
            default -> LATEST;
        };
    }
}
