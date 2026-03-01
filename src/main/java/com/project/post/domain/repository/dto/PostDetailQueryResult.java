package com.project.post.domain.repository.dto;

import java.time.Instant;
import java.util.Set;

public record PostDetailQueryResult(
        Long postId,
        String title,
        String content,
        String thumbnailUrl,
        String authorNickname,
        Long authorId,
        int viewCount,
        int likeCount,
        int scrapCount,
        int commentCount,
        Instant createdAt,
        Instant updatedAt,
        Set<String> tagNames,
        Set<AttachmentDto> attachments
) {

    public record AttachmentDto(
            String fileUrl,
            String fileName,
            String contentType,
            Long fileSize,
            Integer sortOrder
    ) {
    }
}
