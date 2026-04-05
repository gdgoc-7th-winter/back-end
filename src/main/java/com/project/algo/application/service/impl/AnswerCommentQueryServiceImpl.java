package com.project.algo.application.service.impl;

import com.project.algo.application.dto.AnswerCommentResponse;
import com.project.algo.application.service.AnswerCommentQueryService;
import com.project.algo.domain.repository.AnswerCommentRepository;
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

    @Override
    public Page<AnswerCommentResponse> getList(Long challengeId, Long answerId, User viewer, Pageable pageable) {
        return answerCommentRepository
                .findByAnswerCodePostId(answerId, pageable)
                .map(AnswerCommentResponse::from);
    }
}
