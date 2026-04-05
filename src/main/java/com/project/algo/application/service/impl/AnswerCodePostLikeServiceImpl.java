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
        // 배타락으로 동시 토글 직렬화
        AnswerCodePost answer = answerCodePostRepository.findByIdWithLock(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "풀이를 찾을 수 없습니다."));

        if (answerCodePostLikeRepository.existsByAnswerCodePostIdAndUserId(answerId, user.getId())) {
            answerCodePostLikeRepository.deleteByAnswerCodePostIdAndUserId(answerId, user.getId());
            answerCodePostRepository.decrementLikeCount(answerId);
            return new AlgoLikeToggleResponse(false, answerCodePostRepository.findLikeCountById(answerId));
        }

        answerCodePostLikeRepository.save(AnswerCodePostLike.of(answer, user));
        answerCodePostRepository.incrementLikeCount(answerId);
        return new AlgoLikeToggleResponse(true, answerCodePostRepository.findLikeCountById(answerId));
    }
}
