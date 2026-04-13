package com.project.ranking.application.service;

import com.project.ranking.application.dto.RankingMetaResponse;
import com.project.ranking.application.support.RankingPeriodKeys;
import com.project.ranking.domain.RankingPeriodType;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class RankingMetaFactory {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public RankingMetaResponse build(RankingPeriodType periodType, String periodKey, Instant calculatedAt) {
        return switch (periodType) {
            case ALL_TIME -> RankingMetaResponse.of(periodType, periodKey, calculatedAt, null, calculatedAt);
            case WEEKLY -> {
                LocalDate monday = RankingPeriodKeys.parseWeekMondayOrThrow(periodKey);
                Instant start = RankingPeriodKeys.weekStartInclusive(monday);
                ZonedDateTime sundayEnd = monday.plusDays(6).atTime(LocalTime.MAX).atZone(SEOUL);
                yield RankingMetaResponse.of(periodType, periodKey, calculatedAt, start, sundayEnd.toInstant());
            }
            case MONTHLY -> {
                YearMonth ym = RankingPeriodKeys.parseYearMonthOrThrow(periodKey);
                Instant start = RankingPeriodKeys.monthStartInclusive(ym);
                ZonedDateTime monthEnd = ym.atEndOfMonth().atTime(LocalTime.MAX).atZone(SEOUL);
                yield RankingMetaResponse.of(periodType, periodKey, calculatedAt, start, monthEnd.toInstant());
            }
        };
    }
}
