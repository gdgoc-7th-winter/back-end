package com.project.contribution.application.service.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.service.ContributionFacade;
import com.project.contribution.application.service.ContributionService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContributionFacadeImpl implements ContributionFacade {

    private final ContributionService contributionService;

    @Override
    public void grantOnProfileInitialSetupCompleted(Long userId, Long referenceId) {
        contributionService.applyActivity(ActivityContext.profileCompleted(userId, referenceId));
    }

    @Override
    public void applyActivity(ActivityContext context) {
        contributionService.applyActivity(context);
    }
}
