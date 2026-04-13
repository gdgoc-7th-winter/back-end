package com.project.ranking.application.service;

import com.project.ranking.application.support.RankingPeriodKeys;
import com.project.ranking.domain.RankingPeriodType;
import com.project.ranking.infrastructure.persistence.RankingSnapshotJdbcRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 일 배치 — 스테이징 적재 → 본 테이블 프로모션. 동일 period 재실행 시 결과 대체.
 * <p>
 * {@link #runDailySnapshotBatch}는 단일 트랜잭션으로 전체·주·월 갱신을 묶어,
 * 기간별 {@code promoteStagingToMain}(DELETE 후 INSERT … SELECT)가 중간에 끊기면
 * 해당 period 본 테이블이 비는 상태가 되지 않도록 한다. 단건 {@code rebuild*} 호출도
 * 각 메서드의 {@code @Transactional}으로 동일하게 한 트랜잭션에서 스테이징 정리까지 완료한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSnapshotBatchService {

    private final RankingSnapshotJdbcRepository jdbc;

    /**
     * 매일 실행: 전체·직전 완료 주·직전 완료 월 스냅샷을 갱신한다.
     */
    @Transactional
    public void runDailySnapshotBatch(Instant calculatedAt) {
        LocalDate today = RankingPeriodKeys.todaySeoul();
        rebuildAllTime(calculatedAt);

        LocalDate lastWeekMon = RankingPeriodKeys.lastCompletedWeekMonday(today);
        String weekKey = RankingPeriodKeys.formatWeekKey(lastWeekMon);
        rebuildWeekly(
                weekKey,
                RankingPeriodKeys.weekStartInclusive(lastWeekMon),
                RankingPeriodKeys.weekEndExclusive(lastWeekMon),
                calculatedAt);

        YearMonth prevMonth = YearMonth.from(today).minusMonths(1);
        String monthKey = prevMonth.toString();
        rebuildMonthly(
                monthKey,
                RankingPeriodKeys.monthStartInclusive(prevMonth),
                RankingPeriodKeys.monthEndExclusive(prevMonth),
                calculatedAt);

        log.info(
                "랭킹 스냅샷 배치 완료 calculatedAt={} weeklyKey={} monthlyKey={}",
                calculatedAt,
                weekKey,
                monthKey);
    }

    @Transactional
    public void rebuildAllTime(Instant calculatedAt) {
        jdbc.deleteStaging(RankingPeriodType.ALL_TIME, RankingPeriodKeys.ALL_TIME_KEY);
        jdbc.insertAllTimeStaging(calculatedAt);
        jdbc.promoteStagingToMain(RankingPeriodType.ALL_TIME, RankingPeriodKeys.ALL_TIME_KEY);
        jdbc.deleteStagingAfterPromote(RankingPeriodType.ALL_TIME, RankingPeriodKeys.ALL_TIME_KEY);
    }

    @Transactional
    public void rebuildWeekly(String periodKey, Instant occurredStart, Instant occurredEndExclusive, Instant calculatedAt) {
        jdbc.deleteStaging(RankingPeriodType.WEEKLY, periodKey);
        jdbc.insertWeeklyStaging(occurredStart, occurredEndExclusive, periodKey, calculatedAt);
        jdbc.promoteStagingToMain(RankingPeriodType.WEEKLY, periodKey);
        jdbc.deleteStagingAfterPromote(RankingPeriodType.WEEKLY, periodKey);
    }

    @Transactional
    public void rebuildMonthly(String periodKey, Instant occurredStart, Instant occurredEndExclusive, Instant calculatedAt) {
        jdbc.deleteStaging(RankingPeriodType.MONTHLY, periodKey);
        jdbc.insertMonthlyStaging(occurredStart, occurredEndExclusive, periodKey, calculatedAt);
        jdbc.promoteStagingToMain(RankingPeriodType.MONTHLY, periodKey);
        jdbc.deleteStagingAfterPromote(RankingPeriodType.MONTHLY, periodKey);
    }
}
