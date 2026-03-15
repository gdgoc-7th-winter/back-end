package com.project.post.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Campus {

    SEOUL("서울"),
    GLOBAL("글로벌");

    private final String displayName;

    Campus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Campus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        for (Campus campus : values()) {
            if (campus.name().equalsIgnoreCase(normalized)
                    || campus.displayName.equals(normalized)) {
                return campus;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 캠퍼스입니다: " + value);
    }
}
