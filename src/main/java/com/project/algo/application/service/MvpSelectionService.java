package com.project.algo.application.service;

import java.time.LocalDate;

public interface MvpSelectionService {

    /**
     * 지정 날짜에 등록된 모든 DailyChallenge에 대해 MVP를 선정하여 저장한다.
     * 스케줄러 외에 수동 재실행(backfill)에도 사용할 수 있도록 날짜를 파라미터로 받는다.
     */
    void selectDailyMvps(LocalDate targetDate);
}
