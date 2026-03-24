package com.project.post.application.dto.LecturePost;

import com.project.post.application.dto.PostAuthorResponse;
import com.project.post.application.dto.PostViewerResponse;
import com.project.post.domain.enums.Campus;

import java.time.Instant;
import java.util.List;

public record LecturePostListResponse(
        Long postId,
        String title,
        String thumbnailUrl,
        PostAuthorResponse author,
        String department,
        Campus campus,
        long viewCount,
        long likeCount,
        long scrapCount,
        long commentCount,
        PostViewerResponse viewer,
        List<String> tagNames,
        Instant createdAt
) {
}
