package com.project.user.domain.service;

import com.project.user.domain.entity.LevelBadge;

public interface LevelBadgeResolver {

    LevelBadge resolveForTotalPoints(int totalPoint);
}
