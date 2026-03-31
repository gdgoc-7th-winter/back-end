package com.project.user.domain.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.domain.service.LevelBadgeResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LevelBadgeResolverImpl implements LevelBadgeResolver {

    private final LevelBadgeRepository levelBadgeRepository;

    @Override
    public LevelBadge resolveForTotalPoints(int totalPoint) {
        return levelBadgeRepository.findByPointWithinRange(totalPoint)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "해당 점수 구간 뱃지 없음"));
    }
}
