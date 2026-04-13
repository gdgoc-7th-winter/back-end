package com.project.ranking.application.service;

import com.project.ranking.application.support.RankingPeriodKeys;
import com.project.ranking.domain.RankingPeriodType;
import com.project.ranking.infrastructure.persistence.RankingBatchPeriodLock;
import com.project.ranking.infrastructure.persistence.RankingSnapshotJdbcRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 스냅샷 재구성의 트랜잭션 경계. 스테이징 적재 → 본 테이블 프로모션·정리를 기간 단위로 원자적으로 수행한다.
 * <p>
 * {@link RankingSnapshotBatchService}는 일일 순서·키 계산만 담당하고, 실제 작업은 이 클래스를 호출한다.
 * PostgreSQL에서는 기간별 {@link RankingBatchPeriodLock}으로 동시 실행을 막는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSnapshotRebuildService {

    private final RankingSnapshotJdbcRepository jdbc;
    private final RankingBatchPeriodLock periodLock;

    @Transactional
    public void rebuildAllTime(Instant calculatedAt) {
        periodLock.acquire(RankingPeriodType.ALL_TIME, RankingPeriodKeys.ALL_TIME_KEY);
        try {
            jdbc.deleteStaging(RankingPeriodType.ALL_TIME, RankingPeriodKeys.ALL_TIME_KEY);
            jdbc.insertAllTimeStaging(calculatedAt);
            jdbc.promoteStagingToMain(RankingPeriodType.ALL_TIME, RankingPeriodKeys.ALL_TIME_KEY);
            jdbc.deleteStagingAfterPromote(RankingPeriodType.ALL_TIME, RankingPeriodKeys.ALL_TIME_KEY);
        } catch (RuntimeException e) {
            log.error(
                    "랭킹 스냅샷 배치 실패 periodType={} periodKey={} calculatedAt={}",
                    RankingPeriodType.ALL_TIME,
                    RankingPeriodKeys.ALL_TIME_KEY,
                    calculatedAt,
                    e);
            throw e;
        }
    }

    @Transactional
    public void rebuildWeekly(String periodKey, Instant occurredStart, Instant occurredEndExclusive, Instant calculatedAt) {
        periodLock.acquire(RankingPeriodType.WEEKLY, periodKey);
        try {
            jdbc.deleteStaging(RankingPeriodType.WEEKLY, periodKey);
            jdbc.insertWeeklyStaging(occurredStart, occurredEndExclusive, periodKey, calculatedAt);
            jdbc.promoteStagingToMain(RankingPeriodType.WEEKLY, periodKey);
            jdbc.deleteStagingAfterPromote(RankingPeriodType.WEEKLY, periodKey);
        } catch (RuntimeException e) {
            log.error(
                    "랭킹 스냅샷 배치 실패 periodType={} periodKey={} calculatedAt={}",
                    RankingPeriodType.WEEKLY,
                    periodKey,
                    calculatedAt,
                    e);
            throw e;
        }
    }

    @Transactional
    public void rebuildMonthly(String periodKey, Instant occurredStart, Instant occurredEndExclusive, Instant calculatedAt) {
        periodLock.acquire(RankingPeriodType.MONTHLY, periodKey);
        try {
            jdbc.deleteStaging(RankingPeriodType.MONTHLY, periodKey);
            jdbc.insertMonthlyStaging(occurredStart, occurredEndExclusive, periodKey, calculatedAt);
            jdbc.promoteStagingToMain(RankingPeriodType.MONTHLY, periodKey);
            jdbc.deleteStagingAfterPromote(RankingPeriodType.MONTHLY, periodKey);
        } catch (RuntimeException e) {
            log.error(
                    "랭킹 스냅샷 배치 실패 periodType={} periodKey={} calculatedAt={}",
                    RankingPeriodType.MONTHLY,
                    periodKey,
                    calculatedAt,
                    e);
            throw e;
        }
    }
}
