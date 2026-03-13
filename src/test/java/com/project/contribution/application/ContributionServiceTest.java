package com.project.contribution.application;

import com.project.contribution.application.service.impl.ContributionServiceImpl;
import com.project.contribution.domain.entity.ContributionBadge;
import com.project.contribution.domain.repository.ContributionBadgeRepository;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;

import com.project.user.application.service.LevelBadgeService;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock private UserContributionRepository userContributionRepository;
    @Mock private UserRepository userRepository;
    @Mock private ContributionBadgeRepository badgeRepository;
    @Mock private UserService userService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private LevelBadgeService levelBadgeService;

    @Mock private ContributionPolicy mockPolicy;
    private ContributionServiceImpl badgeService;

    @BeforeEach
    void setUp() {
        // List에 Mock 정책을 담아서 직접 생성자 주입
        List<ContributionPolicy> policyList = List.of(mockPolicy);

        // 직접 생성자를 호출하여 Mock들을 전달합니다.
        badgeService = new ContributionServiceImpl(
                policyList,
                userRepository,
                userService,
                badgeRepository,
                userContributionRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("조건을 만족하고 획득 기록이 없는 경우 뱃지를 부여한다")
    void checkAndGrantBadgesSuccess() {
        // Given
        Long userId = 1L;
        String badgeName = "Early Bird";
        ActivityType activityType = ActivityType.POST_CREATED;
        ContributionBadge mockBadge = new ContributionBadge(badgeName, "mockbadge", "mockimage.png", 30);

        User mockUser = new User("test@email.com", "password123");
        ReflectionTestUtils.setField(mockUser, "id", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        when(mockPolicy.supports(activityType)).thenReturn(true);
        when(mockPolicy.getBadge()).thenReturn(mockBadge);
        when(mockPolicy.isSatisfied(userId)).thenReturn(true);

        // 1. 기존에 획득한 적이 없어야 함
        when(userContributionRepository.existsByUserIdAndBadgeName(userId, "Early Bird")).thenReturn(false);
        when(mockPolicy.isSatisfied(userId)).thenReturn(true);
        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(mockBadge));

        // When
        badgeService.checkAndGrantBadges(userId, activityType);

        // Then
        // grantBadge 내부 로직이 호출되었는지(spy 혹은 내부 서비스 호출 여부) 확인
        LevelBadge levelBadge = levelBadgeService.getBadgeForUser(mockUser.getTotalPoint());
        verify(mockPolicy, times(1)).isSatisfied(userId);
        Assertions.assertThat(mockUser.getLevelBadge()).isEqualTo(levelBadge);
    }
}
