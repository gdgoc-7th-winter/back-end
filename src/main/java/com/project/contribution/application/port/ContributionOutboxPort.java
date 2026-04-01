package com.project.contribution.application.port;

import com.project.contribution.application.dto.ActivityContext;

/**
 * 본문 트랜잭션과 동일 커밋 경계에서 기여 활동을 Outbox에 적재한다. 실제 점수 반영은 worker가 수행한다.
 */
public interface ContributionOutboxPort {

    void append(ActivityContext context);
}
