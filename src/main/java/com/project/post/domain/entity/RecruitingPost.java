package com.project.post.domain.entity;

import com.project.global.entity.SoftDeleteEntity;
import com.project.post.domain.enums.ApplicationType;
import com.project.post.domain.enums.PromotionCategory;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.enums.RecruitingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "recruitings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AttributeOverride(name = "id", column = @Column(name = "post_id"))
@SQLRestriction("deleted_at IS NULL")
public class RecruitingPost extends SoftDeleteEntity {

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecruitingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false)
    private ApplicationType applicationType;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "deadline_at")
    private Instant deadlineAt;

    public void updateCategory(RecruitingCategory category) {
        if (category != null) {
            this.category = category;
        }
    }

    public void updateStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void updateDeadlineAt(Instant deadlineAt) {
        this.deadlineAt = deadlineAt;
    }

    public RecruitingStatus calculateCurrentStatus() {
        Instant now = Instant.now();

        if (startedAt != null && now.isBefore(startedAt)) {
            return RecruitingStatus.UPCOMING;
        }

        if (deadlineAt != null && now.isAfter(deadlineAt)) {
            return RecruitingStatus.CLOSED;
        }

        return RecruitingStatus.OPEN;
    }

    public boolean isOpenForApplication() {
        return calculateCurrentStatus() == RecruitingStatus.OPEN;
    }

    public RecruitingStatus getStatus() {
        Instant now = Instant.now();

        if (startedAt != null && now.isBefore(startedAt)) {
            return RecruitingStatus.UPCOMING;
        }

        if (deadlineAt != null && now.isAfter(deadlineAt)) {
            return RecruitingStatus.CLOSED;
        }

        return RecruitingStatus.OPEN;
    }
}
