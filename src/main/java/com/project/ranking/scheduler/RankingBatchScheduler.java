package com.project.ranking.scheduler;

import com.project.ranking.application.service.RankingSnapshotBatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 매일 03:00 KST에 랭킹 스냅샷 배치를 실행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingBatchScheduler {

    private final RankingSnapshotBatchService rankingSnapshotBatchService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void runDailyRankingBatch() {
        try {
            rankingSnapshotBatchService.runDailySnapshotBatch(Instant.now());
        } catch (Exception e) {
            log.error("랭킹 스냅샷 배치 실패", e);
        }
    }
}
