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

import java.util.Optional;

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

        Optional<AnswerCodePostLike> existing =
                answerCodePostLikeRepository.findByAnswerCodePostIdAndUserId(answerId, user.getId());

        boolean liked;
        if (existing.isPresent()) {
            answerCodePostLikeRepository.delete(existing.get());
            answerCodePostRepository.decrementLikeCount(answerId);
            liked = false;
        } else {
            answerCodePostLikeRepository.save(AnswerCodePostLike.of(answer, user));
            answerCodePostRepository.incrementLikeCount(answerId);
            liked = true;
        }

        long updatedLikeCount = answer.getLikeCount() + (liked ? 1 : -1);
        return new AlgoLikeToggleResponse(liked, Math.max(updatedLikeCount, 0));
    }
}
