package com.project.contribution.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "contribution.outbox")
public class ContributionOutboxProperties {

    /** 폴링 주기(ms). */
    private long pollIntervalMs = 500L;

    /** 한 번에 클레임할 최대 건수. */
    private int batchSize = 50;

    /** 처리 실패 시 최대 재시도 횟수(초과 시 DEAD). */
    private int maxAttempts = 8;

    /** PROCESSING이 이 시간(분) 이상 갱신되지 않으면 PENDING으로 되돌린다. */
    private int staleProcessingMinutes = 5;

    /** 오래된 PROCESSING reclaim 주기(ms). */
    private long reclaimIntervalMs = 60_000L;

    /** PostgreSQL FOR UPDATE SKIP LOCKED 사용. */
    private boolean useSkipLocked = true;
}
