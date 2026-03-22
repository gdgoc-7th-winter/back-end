package com.project.post.application.dto.LecturePost;

import com.project.post.application.dto.PostAuthorResponse;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.domain.enums.Campus;

import java.time.Instant;
import java.util.List;

public record LecturePostDetailResponse(
        Long postId,
        String title,
        String content,
        String thumbnailUrl,
        PostAuthorResponse author,
        String department,
        Campus campus,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        Instant createdAt,
        Instant updatedAt,
        List<String> tagNames,
        List<PostDetailResponse.AttachmentResponse> attachments
) {
}
