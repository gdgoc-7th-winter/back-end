package com.project.post.domain.enums;

import java.util.Arrays;

public enum BoardType {

    GENERAL("GENERAL"),

    LECTURE("LECTURE"),

    PROMOTION("PROMOTION");

    private final String code;

    BoardType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static BoardType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 게시판 코드입니다: " + code));
    }
}
