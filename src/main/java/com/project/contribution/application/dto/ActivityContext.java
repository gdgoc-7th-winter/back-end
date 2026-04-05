package com.project.contribution.application.dto;

import com.project.contribution.domain.support.ReferenceKind;
import com.project.global.event.ActivityType;

import org.springframework.lang.Nullable;

import java.time.Instant;

/**
 * 기여 Outbox에 적재되는 활동 문맥. worker가 역직렬화한 뒤 Policy → CommandService 경로로만 처리한다.
 *
 * @param scoreCodeOverride SYSTEM_SCORE_GRANT 등에서 contribution_score.code를 직접 지정할 때만 사용
 */
public record ActivityContext(
        long subjectUserId,
        Long actorUserId,
        ActivityType activityType,
        ReferenceKind referenceKind,
        long referenceId,
        Instant occurredAt,
        @Nullable String scoreCodeOverride
) {

    public static ActivityContext profileCompleted(long userId, long profileReferenceId) {
        return new ActivityContext(
                userId,
                userId,
                ActivityType.PROFILE_SETUP_COMPLETED,
                ReferenceKind.PROFILE,
                profileReferenceId,
                Instant.now(),
                null);
    }

    public static ActivityContext postCreated(long authorId, long postId) {
        return new ActivityContext(
                authorId,
                authorId,
                ActivityType.POST_CREATED,
                ReferenceKind.POST,
                postId,
                Instant.now(),
                null);
    }

    public static ActivityContext postDeleted(long authorId, long postId) {
        return new ActivityContext(
                authorId,
                authorId,
                ActivityType.POST_DELETED,
                ReferenceKind.POST,
                postId,
                Instant.now(),
                null);
    }

    public static ActivityContext commentWritten(long commentAuthorId, long commentId) {
        return new ActivityContext(
                commentAuthorId,
                commentAuthorId,
                ActivityType.COMMENT_WRITTEN,
                ReferenceKind.COMMENT,
                commentId,
                Instant.now(),
                null);
    }

    public static ActivityContext commentDeleted(long commentAuthorId, long commentId) {
        return new ActivityContext(
                commentAuthorId,
                commentAuthorId,
                ActivityType.COMMENT_DELETED,
                ReferenceKind.COMMENT,
                commentId,
                Instant.now(),
                null);
    }

    public static ActivityContext likeReceived(long postAuthorId, long postLikeId, long likerUserId) {
        return new ActivityContext(
                postAuthorId,
                likerUserId,
                ActivityType.LIKE_PRESSED,
                ReferenceKind.LIKE,
                postLikeId,
                Instant.now(),
                null);
    }

    public static ActivityContext scrapReceived(long postAuthorId, long postScrapId, long scraperUserId) {
        return new ActivityContext(
                postAuthorId,
                scraperUserId,
                ActivityType.SCRAP_PRESSED,
                ReferenceKind.SCRAP,
                postScrapId,
                Instant.now(),
                null);
    }

    public static ActivityContext systemScoreGrant(long userId, String scoreCode, long referenceId, Instant occurredAt) {
        return new ActivityContext(
                userId,
                userId,
                ActivityType.SYSTEM_SCORE_GRANT,
                ReferenceKind.SYSTEM,
                referenceId,
                occurredAt,
                scoreCode);
    }
}
