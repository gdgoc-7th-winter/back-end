package com.project.post.application.dto;

import java.time.Instant;
import java.util.List;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        String thumbnailUrl,
        PostAuthorResponse author,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        PostViewerResponse viewer,
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
