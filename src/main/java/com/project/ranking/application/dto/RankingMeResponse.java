package com.project.ranking.application.dto;

import com.project.ranking.domain.RankingPeriodType;

import java.time.Instant;

public record RankingMeResponse(
        RankingPeriodType periodType,
        String periodKey,
        Instant calculatedAt,
        Instant periodStart,
        Instant periodEnd,
        boolean isRealtime,
        int displayRank,
        Integer originalRank,
        long score,
        long userId,
        String nickname,
        String departmentName,
        String levelBadgeImageUrl
) {
}
