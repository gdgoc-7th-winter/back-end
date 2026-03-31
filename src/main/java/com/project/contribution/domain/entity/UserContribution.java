package com.project.contribution.domain.entity;

import com.project.contribution.domain.ContributionEntryType;
import com.project.global.entity.AuditEntity;
import com.project.global.event.ActivityType;
import com.project.user.domain.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_contribution")
public class UserContribution extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contribution_id", nullable = false)
    private ContributionScore contributionScore;

    @Column(nullable = false, name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ContributionEntryType entryType;

    @Column(nullable = false, name = "signed_point")
    private Integer signedPoint;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private ActivityType activityType;

    @Column(length = 64)
    private String referenceType;

    public static UserContribution grant(User user, ContributionScore contributionScore, Long referenceId,
            Instant occurredAt, ActivityType activityType, String idempotencyKey) {
        return UserContribution.builder()
                .user(user)
                .contributionScore(contributionScore)
                .referenceId(referenceId)
                .entryType(ContributionEntryType.GRANT)
                .signedPoint(contributionScore.getPoint())
                .occurredAt(occurredAt)
                .activityType(activityType)
                .idempotencyKey(idempotencyKey)
                .build();
    }

    /**
     * @param revokeMagnitudePositive 회수하려는 점수(양수). 총점보다 크면 원장에는 실제로 깎인 양만 반영한다.
     */
    public static UserContribution revoke(User user, ContributionScore contributionScore, Long referenceId,
            Instant occurredAt, ActivityType activityType, String idempotencyKey, int revokeMagnitudePositive) {
        int magnitude = Math.max(0, revokeMagnitudePositive);
        return UserContribution.builder()
                .user(user)
                .contributionScore(contributionScore)
                .referenceId(referenceId)
                .entryType(ContributionEntryType.REVOKE)
                .signedPoint(-magnitude)
                .occurredAt(occurredAt)
                .activityType(activityType)
                .idempotencyKey(idempotencyKey)
                .build();
    }
}
