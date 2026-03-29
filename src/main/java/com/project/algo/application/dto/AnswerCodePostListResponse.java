package com.project.algo.application.dto;

import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.enums.AlgorithmTag;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record AnswerCodePostListResponse(
        Long answerId,
        String authorNickname,
        Long authorId,
        String languageName,
        String timeComplexity,
        Integer runtime,
        List<AlgorithmTag> algorithmTags,
        long likeCount,
        Instant createdAt
) {
    /**
     * @param answer author 필드가 로드된 상태여야 합니다.
     *               서비스 레이어에서 @BatchSize 또는 fetch join으로 반드시 로드 후 호출하세요.
     */
    public static AnswerCodePostListResponse from(AnswerCodePost answer) {
        Objects.requireNonNull(answer.getAuthor(), "author must be loaded before mapping");
        return new AnswerCodePostListResponse(
                answer.getId(),
                answer.getAuthor().getNickname(),
                answer.getAuthor().getId(),
                answer.getLanguage().getDisplayName(),
                answer.getTimeComplexity(),
                answer.getRuntime(),
                List.copyOf(answer.getAlgorithmTags()),
                answer.getLikeCount(),
                answer.getCreatedAt()
        );
    }
}
