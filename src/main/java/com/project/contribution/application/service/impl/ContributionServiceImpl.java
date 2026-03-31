package com.project.contribution.application.service.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.dto.ContributionPointCommand;
import com.project.contribution.application.service.ContributionCommandService;
import com.project.contribution.application.service.ContributionService;
import com.project.contribution.policy.ContributionPolicy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {

    private final List<ContributionPolicy> policies;
    private final ContributionCommandService contributionCommandService;

    @Override
    @Transactional
    public void applyActivity(ActivityContext context) {
        List<ContributionPointCommand> commands = policies.stream()
                .filter(policy -> policy.supports(context.activityType()))
                .flatMap(policy -> policy.evaluate(context).stream())
                .toList();
        if (commands.isEmpty()) {
            log.debug("No contribution commands for activity {}", context.activityType());
            return;
        }
        for (ContributionPointCommand cmd : commands) {
            switch (cmd.kind()) {
                case GRANT -> contributionCommandService.grantScore(
                        cmd.subjectUserId(),
                        cmd.scoreCode(),
                        cmd.referenceId(),
                        cmd.activityType());
                case REVOKE -> contributionCommandService.revokeScore(
                        cmd.subjectUserId(),
                        cmd.scoreCode(),
                        cmd.referenceId(),
                        cmd.activityType(),
                        cmd.revokeReasonToken());
            }
        }
    }
}
