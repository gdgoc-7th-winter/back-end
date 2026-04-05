package com.project.algo.application.service.impl;

import com.project.algo.application.dto.AnswerCodePostDetailResponse;
import com.project.algo.application.dto.AnswerCodePostListResponse;
import com.project.algo.application.service.AnswerCodePostQueryService;
import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.repository.AnswerCodePostRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerCodePostQueryServiceImpl implements AnswerCodePostQueryService {

    private final AnswerCodePostRepository answerCodePostRepository;

    @Override
    public Page<AnswerCodePostListResponse> getList(Long challengeId, User viewer, Pageable pageable) {
        return answerCodePostRepository
                .findByDailyChallengeId(challengeId, pageable)
                .map(AnswerCodePostListResponse::from);
    }

    @Override
    public AnswerCodePostDetailResponse getDetail(Long challengeId, Long answerId, User viewer) {
        AnswerCodePost answer = answerCodePostRepository.findWithDetailById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "풀이를 찾을 수 없습니다."));
        if (!answer.getDailyChallenge().getId().equals(challengeId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "풀이를 찾을 수 없습니다.");
        }
        return AnswerCodePostDetailResponse.from(answer);
    }
}
