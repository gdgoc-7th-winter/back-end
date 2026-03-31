package com.project.algo.application.service;

import com.project.algo.application.dto.AnswerCommentResponse;
import com.project.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnswerCommentQueryService {

    /** 풀이 제출 여부 검증 후 코멘트 목록 반환 */
    Page<AnswerCommentResponse> getList(Long challengeId, Long answerId, User viewer, Pageable pageable);
}
