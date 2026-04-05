package com.project.algo.application.service.impl;

import com.project.algo.application.dto.DailyChallengeCreateRequest;
import com.project.algo.application.dto.DailyChallengeUpdateRequest;
import com.project.algo.application.service.DailyChallengeCommandService;
import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.repository.DailyChallengeRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyChallengeCommandServiceImpl implements DailyChallengeCommandService {

    private final DailyChallengeRepository dailyChallengeRepository;

    @Override
    @Transactional
    public Long create(@NonNull DailyChallengeCreateRequest request, @NonNull User author) {
        validateCreatePermission(author);

        DailyChallenge challenge = DailyChallenge.builder()
                .author(author)
                .title(request.title())
                .sourceSite(request.sourceSite())
                .problemNumber(request.problemNumber())
                .difficulty(request.difficulty())
                .originalUrl(request.originalUrl())
                .description(request.description())
                .inputFormat(request.inputFormat())
                .outputFormat(request.outputFormat())
                .build();

        if (request.algorithmTags() != null) {
            challenge.getAlgorithmTags().addAll(request.algorithmTags());
        }


        return dailyChallengeRepository.save(challenge).getId();
    }

    @Override
    @Transactional
    public void update(@NonNull Long challengeId, @NonNull DailyChallengeUpdateRequest request, @NonNull User author) {
        DailyChallenge challenge = dailyChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "코테 문제를 찾을 수 없습니다."));

        if (!challenge.getAuthor().getId().equals(author.getId())
                && author.getAuthority() != Authority.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "수정 권한이 없습니다.");
        }

        challenge.update(
                request.title(),
                request.difficulty(),
                request.description(),
                request.inputFormat(),
                request.outputFormat(),
                request.algorithmTags()
        );
    }

    @Override
    @Transactional
    public void delete(@NonNull Long challengeId, @NonNull User author) {
        DailyChallenge challenge = dailyChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "코테 문제를 찾을 수 없습니다."));

        if (!challenge.getAuthor().getId().equals(author.getId())
                && author.getAuthority() != Authority.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "삭제 권한이 없습니다.");
        }

        challenge.softDelete();
    }

    /** DUMMY를 제외한 모든 로그인 사용자가 문제 등록 가능 */
    private void validateCreatePermission(User author) {
        if (author.getAuthority() == Authority.DUMMY) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "문제 등록 권한이 없습니다.");
        }
    }
}
