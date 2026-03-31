package com.project.contribution.application.service.impl;

import com.project.contribution.policy.ContributionPolicy;
import com.project.contribution.application.service.ContributionService;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.event.ActivityType;
import com.project.user.application.dto.EarnScoreResult;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.User;
import com.project.user.event.UserPointChangeEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {
    private final List<ContributionPolicy> policies;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndGrantScores(Long userId, ActivityType activityType, Long referenceId) {
        policies.stream()
                .filter(policy -> policy.supports(activityType))
                .filter(policy -> policy.isSatisfied(userId))
                .forEach(policy -> {
                    try {
                        String scoreName = policy.getScore().getName();
                        grantScore(userId, scoreName, referenceId);

                    } catch (BusinessException e) {
                            if (e.getErrorCode() == ErrorCode.DUPLICATED_ADDRESS) {
                                log.info("중복 요청 무시: User {}, Score {}", userId, policy.getScore().getName());
                            } else {
                                log.error("점수 부여 중 오류 발생: {}", e.getMessage());
                            }
                        }
                });
    }

    @Override
    @Transactional
    public void grantScore(Long id, String scoreName, Long referenceId) {
        EarnScoreResult result = userService.earnAScore(id, scoreName, referenceId);
        if (result.grantedNewLedger()) {
            User updatedUser = result.user();
            eventPublisher.publishEvent(new UserPointChangeEvent(updatedUser.getId(), updatedUser.getTotalPoint()));
        }
    }
}
