package com.project.contribution.application.service.impl;

import com.project.contribution.application.service.ContributionCommandService;
import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.entity.UserContribution;
import com.project.contribution.domain.repository.ContributionScoreRepository;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.project.contribution.domain.support.IdempotencyKeys;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.event.ActivityType;
import com.project.user.application.dto.EarnScoreResult;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import com.project.user.domain.service.LevelBadgeResolver;
import com.project.user.event.UserPointChangeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContributionCommandServiceImpl implements ContributionCommandService {

    private final UserRepository userRepository;
    private final UserContributionRepository userContributionRepository;
    private final ContributionScoreRepository contributionScoreRepository;
    private final LevelBadgeResolver levelBadgeResolver;
    private final ApplicationEventPublisher eventPublisher;

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
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        String idempotencyKey = IdempotencyKeys.grant(user.getId(), contributionScore.getId(), referenceId);
        if (userContributionRepository.existsByIdempotencyKey(idempotencyKey)) {
            return new EarnScoreResult(user, false);
        }

        UserContribution contribution = UserContribution.grant(
                user, contributionScore, referenceId, Instant.now(), activityType, idempotencyKey);

        try {
            userContributionRepository.save(contribution);
            userContributionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateIdempotencyKey(e)) {
                log.debug("Idempotent duplicate user_contribution ignored: {}", idempotencyKey);
                return new EarnScoreResult(user, false);
            }
            log.error("Unexpected error during ledger save for score {}: {}",
                    contributionScore.getCode(), e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "작업 실행 중 오류가 발생했습니다. 관리팀에 문의주세요.");
        }

        int delta = contributionScore.getPoint();
        return applyPointsAndFinish(userId, user, delta, contributionScore.getCode());
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
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        String grantKey = IdempotencyKeys.grant(userId, contributionScore.getId(), referenceId);
        if (!userContributionRepository.existsByIdempotencyKey(grantKey)) {
            log.info("skip revoke — no matching GRANT ledger: userId={}, code={}, ref={}", userId, scoreCode, referenceId);
            return new EarnScoreResult(user, false);
        }

        String revokeKey = IdempotencyKeys.revoke(userId, contributionScore.getId(), referenceId, revokeReasonToken);
        if (userContributionRepository.existsByIdempotencyKey(revokeKey)) {
            return new EarnScoreResult(user, false);
        }

        UserContribution contribution = UserContribution.revoke(
                user, contributionScore, referenceId, Instant.now(), activityType, revokeKey);

        try {
            userContributionRepository.save(contribution);
            userContributionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateIdempotencyKey(e)) {
                log.debug("Idempotent duplicate revoke ignored: {}", revokeKey);
                return new EarnScoreResult(user, false);
            }
            log.error("Unexpected error during revoke ledger for score {}: {}",
                    contributionScore.getCode(), e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "작업 실행 중 오류가 발생했습니다. 관리팀에 문의주세요.");
        }

        int delta = -Math.abs(contributionScore.getPoint());
        return applyPointsAndFinish(userId, user, delta, contributionScore.getCode());
    }

    private EarnScoreResult applyPointsAndFinish(Long userId, User userBefore, int delta, String scoreCodeForLog) {
        int updatedRows = userRepository.addTotalPoints(userId, delta);
        if (updatedRows == 0) {
            log.warn("total_point update skipped or insufficient: userId={}, delta={}, code={}", userId, delta, scoreCodeForLog);
            throw new BusinessException(ErrorCode.INVALID_INPUT, "점수를 반영할 수 없습니다. 총점이 부족하거나 회원 상태를 확인해 주세요.");
        }

        User userAfter = userRepository.findActiveByIdLean(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        applyLevelIfNeeded(userAfter);

        eventPublisher.publishEvent(new UserPointChangeEvent(userAfter.getId(), userAfter.getTotalPoint()));
        return new EarnScoreResult(userAfter, true);
    }

    private void applyLevelIfNeeded(User user) {
        LevelBadge resolved = levelBadgeResolver.resolveForTotalPoints(user.getTotalPoint());
        LevelBadge current = user.getLevelBadge();
        if (current == null) {
            user.updateBadge(resolved);
            return;
        }
        if (!Objects.equals(current.getId(), resolved.getId())) {
            user.updateBadge(resolved);
        }
    }

    private static boolean isDuplicateIdempotencyKey(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof ConstraintViolationException cve) {
            String name = cve.getConstraintName();
            if (name != null && name.toLowerCase(Locale.ROOT).contains("idempotency")) {
                return true;
            }
        }
        String msg = cause.getMessage();
        return msg != null && msg.toLowerCase(Locale.ROOT).contains("idempotency");
    }
}
