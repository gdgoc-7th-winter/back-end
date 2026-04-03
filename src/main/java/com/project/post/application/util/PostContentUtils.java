package com.project.post.application.util;

public class PostContentUtils {

    private static final int CONTENT_PREVIEW_LENGTH = 100;

    private PostContentUtils() {}

    public static String withEllipsis(String preview) {
        if (preview == null || preview.isBlank()) {
            return "";
        }

        return preview.length() == CONTENT_PREVIEW_LENGTH
                ? preview + "..."
                : preview;
    }
}
