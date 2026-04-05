package com.project.algo.application.service;

import com.project.algo.application.dto.AlgoLikeToggleResponse;
import com.project.user.domain.entity.User;

public interface AnswerCodePostLikeService {
    /** 좋아요 토글 — 없으면 등록, 있으면 취소 */
    AlgoLikeToggleResponse toggle(Long challengeId, Long answerId, User user);
}
