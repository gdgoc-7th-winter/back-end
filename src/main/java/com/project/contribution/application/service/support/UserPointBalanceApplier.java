package com.project.contribution.application.service.support;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.EarnScoreResult;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPointBalanceApplier {

    private final UserRepository userRepository;
    private final UserLevelBadgeApplier userLevelBadgeApplier;
    private final UserPointChangeNotifier userPointChangeNotifier;

    /**
     * 총점을 갱신하고, 뱃지·도메인 이벤트를 반영한다. 원장은 호출자가 이미 반영한 뒤 호출한다.
     */
    public EarnScoreResult applyDelta(Long userId, int delta, String scoreCodeForLog) {
        int updatedRows = userRepository.addTotalPoints(userId, delta);
        if (updatedRows == 0) {
            log.warn("total_point update skipped or insufficient: userId={}, delta={}, code={}", userId, delta, scoreCodeForLog);
            throw new BusinessException(ErrorCode.INVALID_INPUT, "점수를 반영할 수 없습니다. 총점이 부족하거나 회원 상태를 확인해 주세요.");
        }

        User userAfter = userRepository.findActiveByIdLean(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        userLevelBadgeApplier.applyIfNeeded(userAfter);
        userPointChangeNotifier.notifyPointChanged(userAfter.getId(), userAfter.getTotalPoint());
        return new EarnScoreResult(userAfter, true);
    }
}
