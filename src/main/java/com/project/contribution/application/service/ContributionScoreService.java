package com.project.contribution.application.service;

import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.presentation.dto.ScoreCreateRequest;
import com.project.contribution.presentation.dto.ScoreUpdateRequest;

public interface ContributionScoreService {
    public ContributionScore addScore(ScoreCreateRequest request);
    public ContributionScore getScore(Long id);
    public ContributionScore editScore(Long id, ScoreUpdateRequest request);
    public void deleteScore(Long id);
}
