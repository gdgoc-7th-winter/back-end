package com.project.contribution.application.service;

import com.project.contribution.domain.entity.ContributionBadge;
import com.project.contribution.presentation.dto.BadgeCreateRequest;
import com.project.contribution.presentation.dto.BadgeUpdateRequest;

public interface ContributionBadgeService {
    public ContributionBadge addBadge(Long id, BadgeCreateRequest request);
    public ContributionBadge getBadge(Long id);
    public ContributionBadge editBadge(Long id,  BadgeUpdateRequest request);
    public void deleteBadge(Long id);
}
