package com.project.contribution.application.service.impl;

import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.contribution.application.service.ContributionService;

import com.project.global.event.ActivityType;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.User;
import com.project.contribution.domain.repository.ContributionScoreRepository;
import com.project.user.domain.repository.UserRepository;
import com.project.user.event.UserPointChangeEvent;

import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {
    private final List<ContributionPolicy> policies;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ContributionScoreRepository scoreRepository;
    private final UserContributionRepository userContributionRepository;
    private final ApplicationEventPublisher eventPublisher;
    Logger log;

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndGrantScores(Long userId, ActivityType activityType) {
        String correlationId = UUID.randomUUID().toString();
        try {
            policies.stream()
                    .filter(policy -> policy.supports(activityType))
                    .forEach(policy -> {
                        String scoreName = policy.getScore().getName();
                        if (!userContributionRepository.existsByUserIdAndScoreName(userId, scoreName)) {
                            if (policy.isSatisfied(userId)) {
                                grantScore(userId, scoreName);
                            }
                        }
                    });
        } catch (Exception e) {
            log.error("Score granting failed | CID: {} | User: {} | Activity: {} | Error: {}",
                    correlationId, userId, activityType, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void grantScore(Long id, String scoreName){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        ContributionScore score = scoreRepository.findByName(scoreName)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        User updatedUser = userService.earnAScore(user.getId(), score);
        eventPublisher.publishEvent(new UserPointChangeEvent(updatedUser.getId(), updatedUser.getTotalPoint()));
    }
}
