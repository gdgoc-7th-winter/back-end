package com.project.user.application.service;

import com.project.user.domain.entity.LevelBadge;
import com.project.user.presentation.dto.request.LevelBadgeRequest;

public interface LevelBadgeService {
    public LevelBadge createLevelBadge(LevelBadgeRequest levelBadgeRequest);
    public LevelBadge updateLevelBadge(Long id, LevelBadgeRequest levelBadgeRequest);
    public void deleteLevelBadge(Long id);
    public LevelBadge getBadgeForUser(Integer point);
}
