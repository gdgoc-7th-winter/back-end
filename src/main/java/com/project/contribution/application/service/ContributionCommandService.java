package com.project.contribution.application.service;

import com.project.user.application.dto.EarnScoreResult;

public interface ContributionCommandService {

    EarnScoreResult grantScore(Long userId, String scoreName, Long referenceId);
}
