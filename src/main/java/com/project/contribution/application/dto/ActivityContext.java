package com.project.contribution.application.dto;

import com.project.contribution.domain.support.ReferenceKind;
import com.project.global.event.ActivityType;

import java.time.Instant;

public record ActivityContext(
        long subjectUserId,
        Long actorUserId,
        ActivityType activityType,
        ReferenceKind referenceKind,
        long referenceId,
        Instant occurredAt
) {

    public static ActivityContext profileCompleted(long userId, long profileReferenceId) {
        return new ActivityContext(
                userId,
                userId,
                ActivityType.PROFILE_SETUP_COMPLETED,
                ReferenceKind.PROFILE,
                profileReferenceId,
                Instant.now());
    }

    public static ActivityContext postCreated(long authorId, long postId) {
        return new ActivityContext(
                authorId,
                authorId,
                ActivityType.POST_CREATED,
                ReferenceKind.POST,
                postId,
                Instant.now());
    }

    public static ActivityContext postDeleted(long authorId, long postId) {
        return new ActivityContext(
                authorId,
                authorId,
                ActivityType.POST_DELETED,
                ReferenceKind.POST,
                postId,
                Instant.now());
    }

    public static ActivityContext commentWritten(long commentAuthorId, long commentId) {
        return new ActivityContext(
                commentAuthorId,
                commentAuthorId,
                ActivityType.COMMENT_WRITTEN,
                ReferenceKind.COMMENT,
                commentId,
                Instant.now());
    }

    public static ActivityContext commentDeleted(long commentAuthorId, long commentId) {
        return new ActivityContext(
                commentAuthorId,
                commentAuthorId,
                ActivityType.COMMENT_DELETED,
                ReferenceKind.COMMENT,
                commentId,
                Instant.now());
    }

    public static ActivityContext likeReceived(long postAuthorId, long postLikeId, long likerUserId) {
        return new ActivityContext(
                postAuthorId,
                likerUserId,
                ActivityType.LIKE_PRESSED,
                ReferenceKind.LIKE,
                postLikeId,
                Instant.now());
    }

    public static ActivityContext scrapReceived(long postAuthorId, long postScrapId, long scraperUserId) {
        return new ActivityContext(
                postAuthorId,
                scraperUserId,
                ActivityType.SCRAP_PRESSED,
                ReferenceKind.SCRAP,
                postScrapId,
                Instant.now());
    }
}
