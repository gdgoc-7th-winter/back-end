package com.project.algo.application.dto;

import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.ProgrammingLanguage;

import java.time.Instant;
import java.util.List;

public record AnswerCodePostDetailResponse(
        Long answerId,
        Long challengeId,
        Long authorId,
        String authorNickname,
        ProgrammingLanguage language,
        String languageName,
        String syntaxMode,
        String code,
        String explanation,
        String timeComplexity,
        Integer runtime,
        List<AlgorithmTag> algorithmTags,
        long likeCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static AnswerCodePostDetailResponse from(AnswerCodePost answer) {
        return new AnswerCodePostDetailResponse(
                answer.getId(),
                answer.getDailyChallenge().getId(),
                answer.getAuthor().getId(),
                answer.getAuthor().getNickname(),
                answer.getLanguage(),
                answer.getLanguage().getDisplayName(),
                answer.getLanguage().getSyntaxMode(),
                answer.getCode(),
                answer.getExplanation(),
                answer.getTimeComplexity(),
                answer.getRuntime(),
                List.copyOf(answer.getAlgorithmTags()),
                answer.getLikeCount(),
                answer.getCreatedAt(),
                answer.getUpdatedAt()
        );
    }
}
