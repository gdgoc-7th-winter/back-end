package com.project.contribution.application.service.impl;

import com.project.contribution.presentation.dto.BadgeCreateRequest;
import com.project.contribution.application.service.ContributionBadgeService;
import com.project.contribution.domain.entity.ContributionBadge;
import com.project.contribution.domain.repository.ContributionBadgeRepository;
import com.project.contribution.presentation.dto.BadgeUpdateRequest;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContributionBadgeServiceImpl implements ContributionBadgeService {
    public ContributionBadgeRepository badgeRepository;

    @Override
    @Transactional
    public ContributionBadge addBadge(Long userId, BadgeCreateRequest request) {
        ContributionBadge badge = new ContributionBadge(
                request.badgeName(),
                request.badgeDescription(),
                request.badgeImage(),
                request.point()
        );
        return badgeRepository.save(badge);
    }

    @Override
    @Transactional
    public ContributionBadge getBadge(Long id) {
        ContributionBadge badge = badgeRepository.findById(id)
                .orElseThrow(()-> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 회원 정보입니다."));
        return badge;
    }


    @Override
    public void deleteBadge(Long id) {
        if (!badgeRepository.findById(id).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Badge not exists");
        }
        badgeRepository.delete(badgeRepository.findById(id).get());
    }

    @Override
    public ContributionBadge editBadge(Long id, BadgeUpdateRequest request) {
        ContributionBadge badge = badgeRepository.findById(id).get();
        badge.update(
                request.badgeName(),
                request.badgeDescription(),
                request.badgeImage(),
                request.point()
        );
        return  badgeRepository.save(badge);
    }
}
