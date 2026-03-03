package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.PostListSort;

import java.util.List;
import java.util.Objects;

public record PostSearchCondition(
        String keyword,
        List<String> tagNames,
        PostListSort sort
) {
    public PostSearchCondition {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        keyword = (normalizedKeyword == null || normalizedKeyword.isBlank()) ? null : normalizedKeyword;

        List<String> normalizedTags = tagNames == null ? List.of() : tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();
        tagNames = List.copyOf(normalizedTags);

        sort = sort == null ? PostListSort.LATEST : sort;
    }

    public boolean hasKeyword() {
        return keyword != null;
    }

    public boolean hasTags() {
        return !tagNames.isEmpty();
    }
}
