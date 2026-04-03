package com.project.post.application.util;

public class PostContentUtils {

    private static final int CONTENT_PREVIEW_LENGTH = 100;

    private PostContentUtils() {}

    public static String withEllipsis(String preview) {
        if (preview == null || preview.isBlank()) {
            return "";
        }

        if (preview.length() > CONTENT_PREVIEW_LENGTH) {
            return preview;
        }

        return preview.substring(0, CONTENT_PREVIEW_LENGTH) + "...";
    }
}
