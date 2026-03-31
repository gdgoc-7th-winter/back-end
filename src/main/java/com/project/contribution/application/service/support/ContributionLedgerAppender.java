package com.project.contribution.application.service.support;

import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.entity.UserContribution;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.project.contribution.domain.support.IdempotencyKeys;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.event.ActivityType;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContributionLedgerAppender {

    private final UserContributionRepository userContributionRepository;

    public GrantAppendResult appendGrant(
            User user,
            ContributionScore contributionScore,
            Long referenceId,
            @Nullable ActivityType activityType,
            Instant occurredAt) {
        String idempotencyKey = IdempotencyKeys.grant(user.getId(), contributionScore.getId(), referenceId);
        if (userContributionRepository.existsByIdempotencyKey(idempotencyKey)) {
            return GrantAppendResult.skipped(user);
        }

        UserContribution contribution = UserContribution.grant(
                user, contributionScore, referenceId, occurredAt, activityType, idempotencyKey);

        try {
            userContributionRepository.save(contribution);
            userContributionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateIdempotencyKey(e)) {
                log.debug("Idempotent duplicate user_contribution ignored: {}", idempotencyKey);
                return GrantAppendResult.skipped(user);
            }
            log.error("Unexpected error during ledger save for score {}: {}",
                    contributionScore.getCode(), e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "작업 실행 중 오류가 발생했습니다. 관리팀에 문의주세요.");
        }

        return GrantAppendResult.proceed();
    }

    public RevokeAppendResult appendRevoke(
            User user,
            ContributionScore contributionScore,
            Long referenceId,
            ActivityType activityType,
            String revokeReasonToken,
            Instant occurredAt) {
        String grantKey = IdempotencyKeys.grant(user.getId(), contributionScore.getId(), referenceId);
        if (!userContributionRepository.existsByIdempotencyKey(grantKey)) {
            log.info("skip revoke — no matching GRANT ledger: userId={}, code={}, ref={}",
                    user.getId(), contributionScore.getCode(), referenceId);
            return RevokeAppendResult.skipped(user);
        }

        String revokeKey = IdempotencyKeys.revoke(
                user.getId(), contributionScore.getId(), referenceId, revokeReasonToken);
        if (userContributionRepository.existsByIdempotencyKey(revokeKey)) {
            return RevokeAppendResult.skipped(user);
        }

        int requestedAbs = Math.abs(contributionScore.getPoint());
        int currentTotal = Math.max(0, user.getTotalPoint());
        int actualRevoke = Math.min(requestedAbs, currentTotal);
        if (actualRevoke < requestedAbs) {
            log.info(
                    "partial revoke: userId={}, code={}, ref={}, requested={}, currentTotal={}, applied={}",
                    user.getId(), contributionScore.getCode(), referenceId, requestedAbs, currentTotal, actualRevoke);
        }

        UserContribution contribution = UserContribution.revoke(
                user, contributionScore, referenceId, occurredAt, activityType, revokeKey, actualRevoke);

        try {
            userContributionRepository.save(contribution);
            userContributionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateIdempotencyKey(e)) {
                log.debug("Idempotent duplicate revoke ignored: {}", revokeKey);
                return RevokeAppendResult.skipped(user);
            }
            log.error("Unexpected error during revoke ledger for score {}: {}",
                    contributionScore.getCode(), e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "작업 실행 중 오류가 발생했습니다. 관리팀에 문의주세요.");
        }

        return RevokeAppendResult.proceed(actualRevoke);
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
