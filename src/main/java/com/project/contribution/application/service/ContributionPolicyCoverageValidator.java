package com.project.contribution.application.service;

import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 각 {@link ActivityType}에 대해 정확히 하나의 {@link ContributionPolicy}가 매핑되는지 기동 시 검증한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContributionPolicyCoverageValidator {

    private final List<ContributionPolicy> contributionPolicies;

    @PostConstruct
    void validate() {
        for (ActivityType activityType : ActivityType.values()) {
            long count = contributionPolicies.stream().filter(p -> p.supports(activityType)).count();
            if (count != 1) {
                throw new IllegalStateException(
                        "ContributionPolicy는 각 ActivityType에 정확히 하나씩 매핑되어야 합니다. "
                                + activityType + " -> " + count);
            }
        }
        log.info("ContributionPolicy 커버리지 검증 완료 (ActivityType {}개)", ActivityType.values().length);
    }
}
