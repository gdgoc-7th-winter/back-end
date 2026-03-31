package com.project.contribution.application.service;

import com.project.contribution.application.dto.ActivityContext;

public interface ContributionService {

    /**
     * 정책 평가 후 지급·회수 명령을 순차 실행한다 (동일 트랜잭션).
     */
    void applyActivity(ActivityContext context);
}
