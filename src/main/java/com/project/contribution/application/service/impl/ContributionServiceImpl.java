package com.project.contribution.application.service.impl;

import com.project.contribution.domain.entity.ContributionBadge;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.contribution.application.service.ContributionService;

import com.project.global.event.ActivityType;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.User;
import com.project.contribution.domain.repository.ContributionBadgeRepository;
import com.project.user.domain.repository.UserRepository;
import com.project.user.event.UserPointChangeEvent;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {
    private final List<ContributionPolicy> policies;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ContributionBadgeRepository badgeRepository;
    private final UserContributionRepository userContributionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndGrantBadges(Long userId, ActivityType activityType) {
        policies.stream()
                .filter(policy -> policy.supports(activityType))
                .forEach(policy -> {
                    ContributionBadge badge = policy.getBadge();
                    if (!userContributionRepository.existsByUserIdAndBadgeName(userId, badge.getName())) {

                        // 3. 정책의 세부 조건(예: 10개 이상인가?) 검사
                        if (policy.isSatisfied(userId)) {
                            grantBadge(userId,badge.getName());
                        }
                    }
                });
    }

    @Override
    public void grantBadge(Long id, String badgeName){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        ContributionBadge badge = badgeRepository.findByName(badgeName)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        userService.earnABadge(user.getId(), badge);
        eventPublisher.publishEvent(new UserPointChangeEvent(user.getId(), user.getTotalPoint()));
    }
}
