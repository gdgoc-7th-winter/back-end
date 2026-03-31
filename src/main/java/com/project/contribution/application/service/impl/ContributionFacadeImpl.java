package com.project.contribution.application.service.impl;

import com.project.contribution.application.service.ContributionFacade;
import com.project.contribution.application.service.ContributionService;
import com.project.global.event.ActivityType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContributionFacadeImpl implements ContributionFacade {

    private final ContributionService contributionService;

    @Override
    public void grantOnProfileInitialSetupCompleted(Long userId, Long referenceId) {
        contributionService.checkAndGrantScores(userId, ActivityType.PROFILE_SETUP_COMPLETED, referenceId);
    }
}
