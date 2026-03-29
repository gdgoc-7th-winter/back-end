package com.project.algo.domain.repository;

import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.enums.AlgorithmTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DailyChallengeRepositoryCustom {

    /**
     * keyword + algorithmTags 조합 동적 검색.
     * ElementCollection JOIN 시 중복 행 발생을 방지하기 위해 서브쿼리 방식으로 구현.
     */
    Page<DailyChallenge> searchChallenges(String keyword, List<AlgorithmTag> tags, Pageable pageable);
}
