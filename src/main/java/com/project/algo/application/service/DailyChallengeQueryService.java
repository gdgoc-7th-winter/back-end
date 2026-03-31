package com.project.algo.application.service;

import com.project.algo.application.dto.DailyChallengeDetailResponse;
import com.project.algo.application.dto.DailyChallengeListResponse;
import com.project.algo.domain.enums.AlgorithmTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DailyChallengeQueryService {

    Page<DailyChallengeListResponse> getList(String keyword, List<AlgorithmTag> algorithmTags, Pageable pageable);

    DailyChallengeDetailResponse getDetail(Long challengeId);
}
