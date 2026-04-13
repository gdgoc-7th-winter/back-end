package com.project.ranking.application.dto;

public record RankingEntryResponse(
        int displayRank,
        Integer originalRank,
        long score,
        long userId,
        String nickname,
        String departmentName,
        String levelBadgeImageUrl,
        boolean isMe
) {
}
