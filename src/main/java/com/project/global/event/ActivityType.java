package com.project.global.event;

public enum ActivityType {
    POST_CREATED("게시글 작성"),
    COMMENT_WRITTEN("댓글 작성"),
    LIKE_PRESSED("좋아요 누름"),
    PROFILE_UPDATED("프로필 수정"),
    PROFILE_SETUP_COMPLETED("초기 프로필 설정 완료");

    private final String description;

    ActivityType(String description) {
        this.description = description;
    }
}
