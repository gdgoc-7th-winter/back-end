package com.project.global.event;

public enum ActivityType {
    POST_CREATED("게시글 작성"),
    POST_DELETED("게시글 삭제"),
    COMMENT_WRITTEN("댓글 작성"),
    COMMENT_DELETED("댓글 삭제"),
    LIKE_PRESSED("좋아요 누름"),
    LIKE_CANCELLED("좋아요 취소"),
    PROFILE_UPDATED("프로필 수정"),
    PROFILE_SETUP_COMPLETED("초기 프로필 설정 완료");

    private final String description;

    ActivityType(String description) {
        this.description = description;
    }
}
