package com.project.contribution.application.service.impl;

import com.project.contribution.presentation.dto.ScoreCreateRequest;
import com.project.contribution.application.service.ContributionScoreService;
import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.repository.ContributionScoreRepository;
import com.project.contribution.presentation.dto.ScoreUpdateRequest;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContributionScoreServiceImpl implements ContributionScoreService {
    private final ContributionScoreRepository scoreRepository;

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public ContributionScore addScore(ScoreCreateRequest request) {
        try {
            ContributionScore score = new ContributionScore(
                    request.scoreName(),
                    request.point()
            );
            return scoreRepository.save(score);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "데이터 무결성 위반 에러가 발생했습니다. 다른 인스턴스와 중복되는 부분이 존재하는 지 확인하십시오.");
        }
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public ContributionScore getScore(Long id) {
        ContributionScore score = scoreRepository.findById(id)
                .orElseThrow(()-> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        return score;
    }


    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public void deleteScore(Long id) {
        ContributionScore score = scoreRepository.findById(id)
                        .orElseThrow(()-> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "score not exists."));
        scoreRepository.delete(score);
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public ContributionScore editScore(Long id, ScoreUpdateRequest request) {
        ContributionScore score = scoreRepository.findById(id)
                        .orElseThrow(()-> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "score not exists"));
        score.update(
                request.scoreName(),
                request.point()
        );
        return  scoreRepository.save(score);
    }
}
