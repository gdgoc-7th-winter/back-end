package com.project.algo.application.dto;

import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.entity.DailyMVP;
import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.CodingTestSite;
import com.project.algo.domain.enums.Difficulty;

import java.time.Instant;
import java.util.List;

public record DailyChallengeDetailResponse(
        Long challengeId,
        String title,
        CodingTestSite sourceSite,
        String problemNumber,
        Difficulty difficulty,
        List<AlgorithmTag> algorithmTags,
        String originalUrl,
        String description,
        String inputFormat,
        String outputFormat,
        Long authorId,
        String authorNickname,
        Instant createdAt,
        Instant updatedAt,
        List<MvpInfo> mvps
) {
    public record MvpInfo(int rank, Long userId, String nickname, long likeCount) {
        public static MvpInfo from(DailyMVP mvp) {
            return new MvpInfo(
                    mvp.getRank(),
                    mvp.getUser().getId(),
                    mvp.getUser().getNickname(),
                    mvp.getLikeCount()
            );
        }
    }

    public static DailyChallengeDetailResponse of(DailyChallenge challenge, List<DailyMVP> mvps) {
        return new DailyChallengeDetailResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getSourceSite(),
                challenge.getProblemNumber(),
                challenge.getDifficulty(),
                List.copyOf(challenge.getAlgorithmTags()),
                challenge.getOriginalUrl(),
                challenge.getDescription(),
                challenge.getInputFormat(),
                challenge.getOutputFormat(),
                challenge.getAuthor().getId(),
                challenge.getAuthor().getNickname(),
                challenge.getCreatedAt(),
                challenge.getUpdatedAt(),
                mvps.stream().map(MvpInfo::from).toList()
        );
    }
}
