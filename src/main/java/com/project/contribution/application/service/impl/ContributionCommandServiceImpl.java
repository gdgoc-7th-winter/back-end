package com.project.contribution.application.service.impl;

import com.project.contribution.application.service.ContributionCommandService;
import com.project.contribution.application.service.support.ContributionLedgerAppender;
import com.project.contribution.application.service.support.GrantAppendResult;
import com.project.contribution.application.service.support.RevokeAppendResult;
import com.project.contribution.application.service.support.UserPointBalanceApplier;
import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.repository.ContributionScoreRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.event.ActivityType;
import com.project.user.application.dto.EarnScoreResult;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContributionCommandServiceImpl implements ContributionCommandService {

    private final UserRepository userRepository;
    private final ContributionScoreRepository contributionScoreRepository;
    private final ContributionLedgerAppender ledgerAppender;
    private final UserPointBalanceApplier userPointBalanceApplier;

    @Override
    @Transactional
    public EarnScoreResult grantScore(Long userId, String scoreCode, Long referenceId) {
        return grantScore(userId, scoreCode, referenceId, null);
    }

    @Override
    @Transactional
    public EarnScoreResult grantScore(Long userId, String scoreCode, Long referenceId, @Nullable ActivityType activityType) {
        User user = userRepository.findActiveByIdLean(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "회원 정보가 없습니다."));
        ContributionScore contributionScore = contributionScoreRepository.findByCode(scoreCode)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "해당 코드의 점수 항목을 찾을 수 없습니다: " + scoreCode));

        GrantAppendResult grantAppend = ledgerAppender.appendGrant(user, contributionScore, referenceId, activityType);
        if (grantAppend.isSkip()) {
            return new EarnScoreResult(grantAppend.skippedWithUser().orElseThrow(), false);
        }

        int delta = contributionScore.getPoint();
        return userPointBalanceApplier.applyDelta(userId, delta, contributionScore.getCode());
    }

    @Override
    @Transactional
    public EarnScoreResult revokeScore(
            Long userId,
            String scoreCode,
            Long referenceId,
            ActivityType activityType,
            String revokeReasonToken) {
        User user = userRepository.findActiveByIdLean(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "회원 정보가 없습니다."));
        ContributionScore contributionScore = contributionScoreRepository.findByCode(scoreCode)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "회수할 점수 항목을 찾을 수 없습니다: " + scoreCode));

        RevokeAppendResult revokeAppend = ledgerAppender.appendRevoke(
                user, contributionScore, referenceId, activityType, revokeReasonToken);
        if (revokeAppend.isSkip()) {
            return new EarnScoreResult(revokeAppend.skippedWithUser().orElseThrow(), false);
        }

        int delta = -revokeAppend.actualRevokeMagnitude();
        return userPointBalanceApplier.applyDelta(userId, delta, contributionScore.getCode());
    }
}
