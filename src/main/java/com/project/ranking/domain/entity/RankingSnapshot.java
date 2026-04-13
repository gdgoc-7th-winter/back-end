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
 * 랭킹 스냅샷 본 테이블. 배치가 기록한 점수·순위(동점은 표준 SQL {@code RANK}와 동일)를 보관한다.
 */
@Entity
@Table(
        name = "ranking_snapshot",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ranking_snapshot_period_user",
                columnNames = {"period_type", "period_key", "user_id"}
        )
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingSnapshot extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private RankingPeriodType periodType;

    @Column(name = "period_key", nullable = false, length = 32)
    private String periodKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "score", nullable = false)
    private long score;

    /**
     * 배치 시점 순위: {@code RANK() OVER (ORDER BY score DESC)} (동점 동순위, 표준 RANK 간격).
     */
    @Column(name = "snapshot_rank", nullable = false)
    private int snapshotRank;

    @Column(name = "calculated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant calculatedAt;
}
