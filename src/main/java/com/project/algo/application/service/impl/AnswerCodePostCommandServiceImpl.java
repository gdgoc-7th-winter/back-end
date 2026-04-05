package com.project.algo.application.service.impl;

import com.project.algo.application.dto.AnswerCodePostCreateRequest;
import com.project.algo.application.dto.AnswerCodePostUpdateRequest;
import com.project.algo.application.service.AnswerCodePostCommandService;
import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.repository.AnswerCodePostRepository;
import com.project.algo.domain.repository.DailyChallengeRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerCodePostCommandServiceImpl implements AnswerCodePostCommandService {

    private final AnswerCodePostRepository answerCodePostRepository;
    private final DailyChallengeRepository dailyChallengeRepository;

    @Override
    @Transactional
    public Long create(@NonNull Long challengeId, @NonNull AnswerCodePostCreateRequest request, @NonNull User author) {
        DailyChallenge challenge = dailyChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "코테 문제를 찾을 수 없습니다."));

        if (answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(challengeId, author.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 풀이를 제출하였습니다.");
        }

        AnswerCodePost answer = AnswerCodePost.builder()
                .dailyChallenge(challenge)
                .author(author)
                .language(request.language())
                .code(request.code())
                .explanation(request.explanation())
                .timeComplexity(request.timeComplexity())
                .runtime(request.runtime())
                .build();

        if (request.algorithmTags() != null) {
            answer.getAlgorithmTags().addAll(request.algorithmTags());
        }

        try {
            return answerCodePostRepository.save(answer).getId();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 풀이를 제출하였습니다.");
        }
    }

    @Override
    @Transactional
    public void update(@NonNull Long answerId, @NonNull AnswerCodePostUpdateRequest request, @NonNull User author) {
        AnswerCodePost answer = answerCodePostRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "풀이를 찾을 수 없습니다."));

        if (!answer.getAuthor().getId().equals(author.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "수정 권한이 없습니다.");
        }

        answer.update(request.language(), request.code(), request.explanation(),
                request.timeComplexity(), request.runtime(), request.algorithmTags());
    }

    @Override
    @Transactional
    public void delete(@NonNull Long answerId, @NonNull User author) {
        AnswerCodePost answer = answerCodePostRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "풀이를 찾을 수 없습니다."));

        if (!answer.getAuthor().getId().equals(author.getId())
                && author.getAuthority() != Authority.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "삭제 권한이 없습니다.");
        }

        answer.softDelete();
    }
}
