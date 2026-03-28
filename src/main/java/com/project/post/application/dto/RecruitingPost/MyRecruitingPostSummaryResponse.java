package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.entity.Post;
import com.project.post.domain.enums.RecruitingStatus;
import com.project.post.domain.enums.RecruitingCategory;

import java.time.Instant;

public record MyRecruitingPostSummaryResponse(
        Long postId,
        String title,
        String thumbnailUrl,
        String content,
        String authorNickname,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Instant createdAt,
        RecruitingStatus status,
        RecruitingCategory category,
        Instant startedAt,
        Instant deadlineAt
) {
    public static MyRecruitingPostSummaryResponse from(RecruitingPost recruitingPost) {
        Post post = recruitingPost.getPost();

        return new MyRecruitingPostSummaryResponse(
                recruitingPost.getId(),
                post.getTitle(),
                post.getThumbnailUrl(),
                post.getContent(),
                post.getAuthor().getNickname(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getCreatedAt(),
                recruitingPost.getStatus(),
                recruitingPost.getCategory(),
                recruitingPost.getStartedAt(),
                recruitingPost.getDeadlineAt()
        );
    }
}