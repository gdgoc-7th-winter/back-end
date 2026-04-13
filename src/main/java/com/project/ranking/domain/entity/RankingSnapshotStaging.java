package com.project.ranking.domain.entity;

import com.project.global.entity.BaseEntity;
import com.project.ranking.domain.RankingPeriodType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * 스테이징 테이블 — 배치 적재 후 본 테이블로 프로모션하고 비운다.
 */
@Entity
@Table(
        name = "ranking_snapshot_staging",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ranking_staging_period_user",
                columnNames = {"period_type", "period_key", "user_id"}
        )
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingSnapshotStaging extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private RankingPeriodType periodType;

    @Column(name = "period_key", nullable = false, length = 32)
    private String periodKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "score", nullable = false)
    private long score;

    @Column(name = "snapshot_rank", nullable = false)
    private int snapshotRank;

    @Column(name = "calculated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant calculatedAt;
}
