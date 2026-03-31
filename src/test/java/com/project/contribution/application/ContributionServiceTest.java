package com.project.contribution.application;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.contribution.application.service.ContributionCommandService;
import com.project.contribution.application.service.impl.ContributionServiceImpl;
import com.project.contribution.domain.support.ContributionScoreCodes;
import com.project.contribution.policy.ContributionPolicy;
import com.project.global.event.ActivityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContributionServiceTest {

    @Mock
    private ContributionCommandService contributionCommandService;
    @Mock
    private ContributionPolicy mockPolicy;

    private ContributionServiceImpl contributionService;

    @BeforeEach
    void setUp() {
        contributionService = new ContributionServiceImpl(List.of(mockPolicy), contributionCommandService);
    }

    @Test
    @DisplayName("지원하지 않는 활동 유형이면 evaluate를 호출하지 않고 원장을 건드리지 않는다")
    void applyActivitySkipsWhenPolicyDoesNotSupport() {
        ActivityContext ctx = ActivityContext.postCreated(1L, 10L);
        when(mockPolicy.supports(ActivityType.POST_CREATED)).thenReturn(false);

        contributionService.applyActivity(ctx);

        verify(mockPolicy, never()).evaluate(any());
        verifyNoInteractions(contributionCommandService);
    }

    @Test
    @DisplayName("정책이 빈 명령 목록을 반환하면 원장을 호출하지 않는다")
    void applyActivityNoOpsWhenCommandsEmpty() {
        ActivityContext ctx = ActivityContext.postCreated(1L, 10L);
        when(mockPolicy.supports(ActivityType.POST_CREATED)).thenReturn(true);
        when(mockPolicy.evaluate(ctx)).thenReturn(List.of());

        contributionService.applyActivity(ctx);

        verifyNoInteractions(contributionCommandService);
    }

    @Test
    @DisplayName("정책이 GRANT 명령을 반환하면 grantScore를 호출한다")
    void applyActivityExecutesGrant() {
        ActivityContext ctx = ActivityContext.postCreated(1L, 10L);
        ContributionPointCommand cmd = ContributionPointCommand.grant(
                1L, ContributionScoreCodes.POST_WRITE, 10L, ActivityType.POST_CREATED);
        when(mockPolicy.supports(ActivityType.POST_CREATED)).thenReturn(true);
        when(mockPolicy.evaluate(ctx)).thenReturn(List.of(cmd));

        contributionService.applyActivity(ctx);

        verify(contributionCommandService).grantScore(1L, ContributionScoreCodes.POST_WRITE, 10L, ActivityType.POST_CREATED);
        verify(contributionCommandService, never()).revokeScore(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("정책이 REVOKE 명령을 반환하면 revokeScore를 호출한다")
    void applyActivityExecutesRevoke() {
        ActivityContext ctx = ActivityContext.postDeleted(1L, 10L);
        ContributionPointCommand cmd = ContributionPointCommand.revoke(
                1L,
                ContributionScoreCodes.POST_WRITE,
                10L,
                ActivityType.POST_DELETED,
                ActivityType.POST_DELETED.name());
        when(mockPolicy.supports(ActivityType.POST_DELETED)).thenReturn(true);
        when(mockPolicy.evaluate(ctx)).thenReturn(List.of(cmd));

        contributionService.applyActivity(ctx);

        verify(contributionCommandService)
                .revokeScore(
                        1L,
                        ContributionScoreCodes.POST_WRITE,
                        10L,
                        ActivityType.POST_DELETED,
                        ActivityType.POST_DELETED.name());
    }
}
