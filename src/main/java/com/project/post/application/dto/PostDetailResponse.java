package com.project.post.application.dto;

import java.time.Instant;
import java.util.List;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        String thumbnailUrl,
        String authorNickname,
        Long authorId,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        Instant createdAt,
        Instant updatedAt,
        List<String> tagNames,
        List<AttachmentResponse> attachments
) {
    public record AttachmentResponse(
            String fileUrl,
            String fileName,
            String contentType,
            Long fileSize,
            int sortOrder
    ) {
    }
}
