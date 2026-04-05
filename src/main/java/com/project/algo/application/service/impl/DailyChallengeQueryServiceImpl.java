package com.project.algo.application.service.impl;

import com.project.algo.application.dto.DailyChallengeDetailResponse;
import com.project.algo.application.dto.DailyChallengeListResponse;
import com.project.algo.application.service.DailyChallengeQueryService;
import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.entity.DailyMVP;
import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.repository.DailyChallengeRepository;
import com.project.algo.domain.repository.DailyMVPRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyChallengeQueryServiceImpl implements DailyChallengeQueryService {

    private final DailyChallengeRepository dailyChallengeRepository;
    private final DailyMVPRepository dailyMVPRepository;

    @Override
    public Page<DailyChallengeListResponse> getList(String keyword, List<AlgorithmTag> algorithmTags, Pageable pageable) {
        return dailyChallengeRepository
                .searchChallenges(keyword, algorithmTags, pageable)
                .map(DailyChallengeListResponse::from);
    }

    @Override
    public DailyChallengeDetailResponse getDetail(Long challengeId) {
        DailyChallenge challenge = dailyChallengeRepository.findWithDetailById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "코테 문제를 찾을 수 없습니다."));

        List<DailyMVP> mvps = dailyMVPRepository.findByDailyChallengeIdOrderByRankAsc(challengeId);

        return DailyChallengeDetailResponse.of(challenge, mvps);
    }
}
