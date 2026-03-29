package com.project.global.response;

public record PageMeta(
        int page,
        int size,
        long totalCount,
        int totalPages
) {
}
