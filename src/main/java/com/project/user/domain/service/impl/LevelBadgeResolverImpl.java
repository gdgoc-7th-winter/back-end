package com.project.user.domain.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.domain.service.LevelBadgeResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LevelBadgeResolverImpl implements LevelBadgeResolver {

    private final LevelBadgeRepository levelBadgeRepository;

    /**
     * 총점 기준 티어를 결정한다.
     * <ul>
     *   <li>음수 총점: DB에서 total_point 음수 갱신이 막히므로 도메인상 거의 없음. 해석 시 0으로 클램프.</li>
     *   <li>구간 매칭: {@code minimum_point ~ maximum_point} 포함 구간.</li>
     *   <li>상한 초과: 최고 {@code maximum_point}를 가진 뱃지로 매핑(티어가 비는 상태 방지).</li>
     * </ul>
     */
    @Override
    public LevelBadge resolveForTotalPoints(int totalPoint) {
        int clamped = Math.max(0, totalPoint);
        Optional<LevelBadge> inRange = levelBadgeRepository.findByPointWithinRange(clamped);
        if (inRange.isPresent()) {
            return inRange.get();
        }

        Optional<LevelBadge> topByMaxPoint = levelBadgeRepository.findFirstByOrderByMaximumPointDesc();
        if (topByMaxPoint.isPresent()) {
            LevelBadge top = topByMaxPoint.get();
            if (clamped > top.getMaximumPoint()) {
                return top;
            }
        }

        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "해당 점수 구간 뱃지 없음(또는 level_badge 구간 설정을 확인하세요)");
    }
}
