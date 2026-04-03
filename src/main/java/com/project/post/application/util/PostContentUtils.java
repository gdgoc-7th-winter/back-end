package com.project.post.application.util;

public final class PostContentUtils {

    private static final int CONTENT_PREVIEW_LENGTH = 100;

    private PostContentUtils() {
    }

    public static String makePreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        return content.length() <= CONTENT_PREVIEW_LENGTH
                ? content
                : content.substring(0, CONTENT_PREVIEW_LENGTH) + "...";
    }
}