package com.project.post.domain.repository.dto;

import com.project.post.domain.enums.Campus;
import com.project.post.domain.enums.PostListSort;

import java.util.List;
import java.util.Objects;

public record LecturePostSearchCondition(
        String keyword,
        List<String> tagNames,
        Campus campus,
        List<String> departments,
        PostListSort sort
) {
    public LecturePostSearchCondition {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        keyword = (normalizedKeyword == null || normalizedKeyword.isBlank()) ? null : normalizedKeyword;

        List<String> normalizedTags = tagNames == null ? List.of() : tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();
        tagNames = List.copyOf(normalizedTags);

        List<String> normalizedDepts = departments == null ? List.of() : departments.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(dept -> !dept.isBlank())
                .distinct()
                .toList();
        departments = List.copyOf(normalizedDepts);

        sort = sort == null ? PostListSort.LATEST : sort;
    }

    public boolean hasKeyword() {
        return keyword != null;
    }

    public boolean hasTags() {
        return !tagNames.isEmpty();
    }

    public boolean hasCampus() {
        return campus != null;
    }

    public boolean hasDepartments() {
        return !departments.isEmpty();
    }
}
