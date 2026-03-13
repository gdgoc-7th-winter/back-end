package com.project.user.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.service.LevelBadgeService;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.presentation.dto.request.LevelBadgeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LevelBadgeServiceImpl implements LevelBadgeService {
    private final LevelBadgeRepository levelBadgeRepository;

    // CREATE
    @Override
    @Transactional
    public LevelBadge createLevelBadge(LevelBadgeRequest request) {
        LevelBadge levelBadge = new LevelBadge(
                request.levelName(),
                request.levelDescription(),
                request.levelImage(),
                request.minimumPoint(),
                request.maximumPoint()
        );
        return levelBadgeRepository.save(levelBadge);
    }

    // READ
    @Override
    @Transactional
    public LevelBadge getBadgeForUser(Integer userPoint) {
        return levelBadgeRepository.findByPointWithinRange(userPoint)
                .orElseThrow(() -> new IllegalArgumentException("해당 점수 구간에 정의된 뱃지가 없습니다: " + userPoint));
    }

    // UPDATE
    @Override
    @Transactional
    public LevelBadge updateLevelBadge(Long id, LevelBadgeRequest request) {
        LevelBadge levelBadge = levelBadgeRepository.findById(id)
                .orElseThrow(()-> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "badge not exists"));

        levelBadge.update(
                request.levelName(),
                request.levelDescription(),
                request.levelImage(),
                request.minimumPoint(),
                request.maximumPoint()
        );
        return levelBadge;
    }

    // DELETE
    @Override
    @Transactional
    public void deleteLevelBadge(Long id) {
        if (!levelBadgeRepository.findById(id).isPresent()){
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Badge not exists");
        }
        levelBadgeRepository.delete(levelBadgeRepository.findById(id).get());
    }
}
