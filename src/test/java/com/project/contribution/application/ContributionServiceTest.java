package com.project.contribution.application;

import com.project.contribution.application.service.ContributionCommandService;
import com.project.contribution.application.service.impl.ContributionServiceImpl;
import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;

import com.project.user.application.dto.EarnScoreResult;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ContributionServiceTest {
    @Mock private ContributionCommandService contributionCommandService;
    @Mock private ContributionPolicy mockPolicy;
    private ContributionServiceImpl scoreService;

    @BeforeEach
    void setUp() {
        List<ContributionPolicy> policyList = List.of(mockPolicy);
        scoreService = new ContributionServiceImpl(
                policyList,
                contributionCommandService
        );
    }

    @Test
    @DisplayName("조건을 만족하고 획득 기록이 없는 경우 뱃지를 부여한다")
    void checkAndGrantBadgesSuccess() {
        Long userId = 1L;
        Long referenceId = 10L;
        String scoreName = "Early Bird";
        ActivityType activityType = ActivityType.POST_CREATED;
        ContributionScore mockScore = new ContributionScore(scoreName, 30);

        User mockUser = User.builder().email("test@email.com").password("password123").nickname("testuser1").build();
        ReflectionTestUtils.setField(mockUser, "id", userId);

        when(mockPolicy.supports(activityType)).thenReturn(true);
        when(mockPolicy.getScore()).thenReturn(mockScore);
        when(mockPolicy.isSatisfied(userId)).thenReturn(true);

        when(contributionCommandService.grantScore(userId, scoreName, referenceId))
                .thenReturn(new EarnScoreResult(mockUser, true));
        scoreService.checkAndGrantScores(userId, activityType, referenceId);

        verify(mockPolicy, times(1)).isSatisfied(userId);
        verify(contributionCommandService, times(1)).grantScore(userId, scoreName, referenceId);
    }
}
