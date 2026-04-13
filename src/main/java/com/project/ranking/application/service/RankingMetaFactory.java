package com.project.ranking.application.service;

import com.project.ranking.application.dto.RankingMetaResponse;
import com.project.ranking.application.support.RankingPeriodKeys;
import com.project.ranking.domain.RankingPeriodType;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * API 메타의 기간 경계는 {@link RankingPeriodKeys}의 inclusive/exclusive 시각과 동일해야 한다.
 */
@Component
public class RankingMetaFactory {

    public RankingMetaResponse build(RankingPeriodType periodType, String periodKey, Instant calculatedAt) {
        return switch (periodType) {
            case ALL_TIME -> RankingMetaResponse.of(periodType, periodKey, calculatedAt, null, calculatedAt);
            case WEEKLY -> {
                LocalDate monday = RankingPeriodKeys.parseWeekMondayOrThrow(periodKey);
                Instant start = RankingPeriodKeys.weekStartInclusive(monday);
                Instant endExclusive = RankingPeriodKeys.weekEndExclusive(monday);
                yield RankingMetaResponse.of(periodType, periodKey, calculatedAt, start, endExclusive);
            }
            case MONTHLY -> {
                YearMonth ym = RankingPeriodKeys.parseYearMonthOrThrow(periodKey);
                Instant start = RankingPeriodKeys.monthStartInclusive(ym);
                Instant endExclusive = RankingPeriodKeys.monthEndExclusive(ym);
                yield RankingMetaResponse.of(periodType, periodKey, calculatedAt, start, endExclusive);
            }
        };
    }
}
