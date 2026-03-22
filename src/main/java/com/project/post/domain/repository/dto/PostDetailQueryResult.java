package com.project.post.domain.repository.dto;

import java.time.Instant;
import java.util.List;

public record PostDetailQueryResult(
        Long postId,
        String title,
        String content,
        String thumbnailUrl,
        Long authorId,
        String authorNickname,
        String authorProfileImgUrl,
        String authorDepartmentName,
        String authorRepresentativeTrackName,
        String authorTierBadgeImageUrl,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        Instant createdAt,
        Instant updatedAt,
        List<String> tagNames,
        List<AttachmentDto> attachments
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
