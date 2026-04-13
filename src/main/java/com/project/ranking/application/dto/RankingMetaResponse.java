package com.project.ranking.application.dto;

import com.project.ranking.domain.RankingPeriodType;

import java.time.Instant;

/**
 * @param periodStart 집계 구간 하한(inclusive). ALL_TIME에서는 null일 수 있음.
 * @param periodEnd   집계 구간 상한. 배치·원장 필터의 {@code occurred_at < periodEnd}와 동일한 <strong>exclusive</strong> 시각.
 */
public record RankingMetaResponse(
        RankingPeriodType periodType,
        String periodKey,
        Instant calculatedAt,
        Instant periodStart,
        Instant periodEnd,
        boolean isRealtime,
        boolean snapshotAvailable
) {

    public static RankingMetaResponse of(
            RankingPeriodType periodType,
            String periodKey,
            Instant calculatedAt,
            Instant periodStart,
            Instant periodEnd) {
        boolean snapshotAvailable = calculatedAt != null;
        return new RankingMetaResponse(periodType, periodKey, calculatedAt, periodStart, periodEnd, false, snapshotAvailable);
    }
}
