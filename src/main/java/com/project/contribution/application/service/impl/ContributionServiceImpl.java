package com.project.contribution.application.service.impl;

import com.project.contribution.application.service.ContributionCommandService;
import com.project.contribution.policy.ContributionPolicy;
import com.project.contribution.application.service.ContributionService;

import com.project.global.event.ActivityType;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {
    private final List<ContributionPolicy> policies;
    private final ContributionCommandService contributionCommandService;

    @Override
    @Transactional
    public void checkAndGrantScores(Long userId, ActivityType activityType, Long referenceId) {
        policies.stream()
                .filter(policy -> policy.supports(activityType))
                .filter(policy -> policy.isSatisfied(userId))
                .forEach(policy -> {
                    String scoreName = policy.getScore().getName();
                    grantScore(userId, scoreName, referenceId);
                });
    }

    @Override
    @Transactional
    public void grantScore(Long id, String scoreName, Long referenceId) {
        contributionCommandService.grantScore(id, scoreName, referenceId);
    }
}
