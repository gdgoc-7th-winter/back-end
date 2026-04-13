package com.project.ranking.application.dto;

public record RankingPageInfo(
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
