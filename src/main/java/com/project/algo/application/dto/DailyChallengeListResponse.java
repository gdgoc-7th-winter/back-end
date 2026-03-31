package com.project.algo.application.dto;

import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.CodingTestSite;
import com.project.algo.domain.enums.Difficulty;

import java.time.Instant;
import java.util.List;

public record DailyChallengeListResponse(
        Long challengeId,
        String title,
        CodingTestSite sourceSite,
        String problemNumber,
        Difficulty difficulty,
        List<AlgorithmTag> algorithmTags,
        String authorNickname,
        Instant createdAt
) {
    public static DailyChallengeListResponse from(DailyChallenge challenge) {
        return new DailyChallengeListResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getSourceSite(),
                challenge.getProblemNumber(),
                challenge.getDifficulty(),
                List.copyOf(challenge.getAlgorithmTags()),
                challenge.getAuthor().getNickname(),
                challenge.getCreatedAt()
        );
    }
}
