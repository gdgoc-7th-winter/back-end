package com.project.algo.scheduler;

import com.project.algo.application.service.MvpSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class MvpScheduler {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final MvpSelectionService mvpSelectionService;

    @Scheduled(cron = "59 59 23 * * *", zone = "Asia/Seoul")
    public void runDailyMvpSelection() {
        LocalDate today = LocalDate.now(SEOUL);
        log.info("[MvpScheduler] MVP 선정 시작 — targetDate={}", today);
        try {
            mvpSelectionService.selectDailyMvps(today);
        } catch (Exception e) {
            log.error("[MvpScheduler] MVP 선정 중 오류 발생 — targetDate={}", today, e);
        }
    }
}
