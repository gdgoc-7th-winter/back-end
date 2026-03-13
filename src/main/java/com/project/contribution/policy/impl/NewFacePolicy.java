package com.project.contribution.policy.impl;

import com.project.contribution.domain.entity.ContributionBadge;
import com.project.contribution.domain.repository.ContributionBadgeRepository;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.event.ActivityType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewFacePolicy implements ContributionPolicy {
    private final ContributionBadgeRepository contributionBadgeRepository;

    @Override
    public boolean supports(ActivityType activityType) {
        return activityType == ActivityType.PROFILE_SETUP_COMPLETED;
    }

    @Override
    public boolean isSatisfied(Long userId) {
        return true;
    }

    @Override
    public ContributionBadge getBadge() {
        return contributionBadgeRepository.findByName("뉴페이스")
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "해당 조건에 맞는 뱃지를 찾을 수 없습니다."));
    }

}
