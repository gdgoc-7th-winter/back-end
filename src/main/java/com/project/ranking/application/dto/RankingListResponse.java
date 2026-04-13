package com.project.ranking.application.dto;

import java.util.List;

public record RankingListResponse(
        RankingMetaResponse meta,
        RankingPageInfo page,
        List<RankingEntryResponse> content
) {
}
