package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.Campus;

import java.time.Instant;

public record LecturePostListQueryResult(
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
        Instant createdAt
) {
}
