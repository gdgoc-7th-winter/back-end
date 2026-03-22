package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.Campus;

import java.time.Instant;
import java.util.List;

public record LecturePostDetailQueryResult(
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
        String department,
        Campus campus,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        Instant createdAt,
        Instant updatedAt,
        List<String> tagNames,
        List<PostDetailQueryResult.AttachmentDto> attachments
) {
}
