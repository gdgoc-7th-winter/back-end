package com.project.ranking.application.dto;

import com.project.ranking.domain.RankingPeriodType;

import java.time.Instant;

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
