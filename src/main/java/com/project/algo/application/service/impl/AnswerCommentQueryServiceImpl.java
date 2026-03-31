package com.project.algo.application.service.impl;

import com.project.algo.application.dto.AnswerCommentResponse;
import com.project.algo.application.service.AnswerCommentQueryService;
import com.project.algo.domain.repository.AnswerCodePostRepository;
import com.project.algo.domain.repository.AnswerCommentRepository;
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
public class AnswerCommentQueryServiceImpl implements AnswerCommentQueryService {

    private final AnswerCommentRepository answerCommentRepository;
    private final AnswerCodePostRepository answerCodePostRepository;

    @Override
    public Page<AnswerCommentResponse> getList(Long challengeId, Long answerId, User viewer, Pageable pageable) {
        if (!answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(challengeId, viewer.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "풀이를 먼저 제출해주세요.");
        }
        return answerCommentRepository
                .findByAnswerCodePostId(answerId, pageable)
                .map(AnswerCommentResponse::from);
    }
}
