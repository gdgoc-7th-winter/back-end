package com.project.algo.application.dto;

import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.enums.AlgorithmTag;

import java.time.Instant;
import java.util.List;

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
    public static AnswerCodePostListResponse from(AnswerCodePost answer) {
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
