package com.project.contribution.infrastructure.persistence.entity;

import com.project.contribution.domain.ContributionOutboxStatus;
import com.project.global.entity.ManualTimestampEntity;
import com.project.global.event.ActivityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "contribution_outbox")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContributionOutboxEntity extends ManualTimestampEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 64)
    private ActivityType activityType;

    @Column(name = "context_payload", nullable = false, columnDefinition = "TEXT")
    private String contextPayload;

    @Column(name = "dedup_key", nullable = false, unique = true, length = 512)
    private String dedupKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ContributionOutboxStatus status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_retry_at", columnDefinition = "TIMESTAMPTZ")
    private Instant nextRetryAt;

    public static ContributionOutboxEntity pending(
            ActivityType activityType,
            String contextPayload,
            String dedupKey,
            Instant now) {
        ContributionOutboxEntity e = new ContributionOutboxEntity();
        e.activityType = activityType;
        e.contextPayload = contextPayload;
        e.dedupKey = dedupKey;
        e.status = ContributionOutboxStatus.PENDING;
        e.attempts = 0;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public void markProcessing(Instant now) {
        this.status = ContributionOutboxStatus.PROCESSING;
        this.updatedAt = now;
    }

    public void markDone(Instant now) {
        this.status = ContributionOutboxStatus.DONE;
        this.lastError = null;
        this.updatedAt = now;
    }

    public void recoverAfterProcessingFailure(String error, int maxAttempts, Instant now) {
        this.attempts++;
        this.lastError = truncate(error);
        this.updatedAt = now;
        if (this.attempts >= maxAttempts) {
            this.status = ContributionOutboxStatus.DEAD;
            this.nextRetryAt = null;
        } else {
            this.status = ContributionOutboxStatus.PENDING;
            long backoffSec = Math.min(300L, 1L << Math.min(this.attempts, 8));
            this.nextRetryAt = now.plusSeconds(backoffSec);
        }
    }

    private static String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() > 2000 ? error.substring(0, 2000) : error;
    }
}
