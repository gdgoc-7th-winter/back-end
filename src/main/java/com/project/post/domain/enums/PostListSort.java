package com.project.post.domain.enums;

public enum PostListSort {
    LATEST,
    VIEWS,
    LIKES;

    public static PostListSort from(String value) {
        if (value == null || value.isBlank()) {
            return LATEST;
        }
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "views", "view" -> VIEWS;
            case "likes", "like" -> LIKES;
            default -> LATEST;
        };
    }
}
