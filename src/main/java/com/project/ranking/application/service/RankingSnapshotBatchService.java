package com.project.ranking.application.service;

import com.project.ranking.application.support.RankingPeriodKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 일 배치 오케스트레이션: 서울 기준 날짜·주·월 키 계산과 ALL_TIME → WEEKLY → MONTHLY 실행 순서만 담당한다.
 * 트랜잭션·락·스테이징/프로모션은 {@link RankingSnapshotRebuildService}에 위임한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSnapshotBatchService {

    private final RankingSnapshotRebuildService rebuildService;

    /**
     * 매일 실행: 전체·직전 완료 주·직전 완료 월 스냅샷을 갱신한다.
     */
    public void runDailySnapshotBatch(Instant calculatedAt) {
        LocalDate today = RankingPeriodKeys.todaySeoul();
        rebuildService.rebuildAllTime(calculatedAt);

        LocalDate lastWeekMon = RankingPeriodKeys.lastCompletedWeekMonday(today);
        String weekKey = RankingPeriodKeys.formatWeekKey(lastWeekMon);
        rebuildService.rebuildWeekly(
                weekKey,
                RankingPeriodKeys.weekStartInclusive(lastWeekMon),
                RankingPeriodKeys.weekEndExclusive(lastWeekMon),
                calculatedAt);

        YearMonth prevMonth = YearMonth.from(today).minusMonths(1);
        String monthKey = prevMonth.toString();
        rebuildService.rebuildMonthly(
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
}
