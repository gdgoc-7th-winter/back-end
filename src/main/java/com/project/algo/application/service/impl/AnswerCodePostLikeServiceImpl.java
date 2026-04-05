package com.project.algo.application.service.impl;

import com.project.algo.application.dto.AlgoLikeToggleResponse;
import com.project.algo.application.service.AnswerCodePostLikeService;
import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.entity.AnswerCodePostLike;
import com.project.algo.domain.repository.AnswerCodePostLikeRepository;
import com.project.algo.domain.repository.AnswerCodePostRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerCodePostLikeServiceImpl implements AnswerCodePostLikeService {

    private final AnswerCodePostRepository answerCodePostRepository;
    private final AnswerCodePostLikeRepository answerCodePostLikeRepository;

    @Override
    @Transactional
    public AlgoLikeToggleResponse toggle(Long challengeId, Long answerId, User user) {
        // 풀이 제출 여부 검증
        if (!answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(challengeId, user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "풀이를 먼저 제출해주세요.");
        }

        AnswerCodePost answer = answerCodePostRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "풀이를 찾을 수 없습니다."));

        int deleted = answerCodePostLikeRepository.deleteByAnswerCodePostIdAndUserId(answerId, user.getId());
        if (deleted > 0) {
            answerCodePostRepository.decrementLikeCount(answerId);
            return new AlgoLikeToggleResponse(false, Math.max(answer.getLikeCount() - 1, 0));
        }

        try {
            answerCodePostLikeRepository.saveAndFlush(AnswerCodePostLike.of(answer, user));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 좋아요를 누른 풀이입니다.");
        }
        answerCodePostRepository.incrementLikeCount(answerId);
        return new AlgoLikeToggleResponse(true, answer.getLikeCount() + 1);
    }
}
