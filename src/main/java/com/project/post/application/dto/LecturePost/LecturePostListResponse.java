package com.project.post.application.dto.LecturePost;

import com.project.post.domain.enums.Campus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record LecturePostListResponse(
        Long postId,
        String title,
        String thumbnailUrl,
        String authorNickname,
        String department,
        Campus campus,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        @Schema(description = "게시글 태그 목록")
        List<String> tagNames,
        Instant createdAt
) {
}
