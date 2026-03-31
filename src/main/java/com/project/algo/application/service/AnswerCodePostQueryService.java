package com.project.algo.application.service;

import com.project.algo.application.dto.AnswerCodePostDetailResponse;
import com.project.algo.application.dto.AnswerCodePostListResponse;
import com.project.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnswerCodePostQueryService {

    /** 풀이 목록 조회 — 해당 문제에 본인 풀이 제출 여부 검증 포함 */
    Page<AnswerCodePostListResponse> getList(Long challengeId, User viewer, Pageable pageable);

    /** 풀이 상세 조회 — 해당 문제에 본인 풀이 제출 여부 검증 포함 */
    AnswerCodePostDetailResponse getDetail(Long challengeId, Long answerId, User viewer);
}
