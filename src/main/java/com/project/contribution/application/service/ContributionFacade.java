package com.project.contribution.application.service;

import com.project.contribution.application.dto.ActivityContext;

public interface ContributionFacade {

    void grantOnProfileInitialSetupCompleted(Long userId, Long referenceId);

    void applyActivity(ActivityContext context);
}
