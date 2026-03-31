package com.project.global.event;

/**
 * contribution_score와 연결되는 활동.
 * 회수(REVOKE)는 작성자 본인이 게시글/댓글을 삭제할 때만 발생한다.
 * 타인의 좋아요·스크랩 취소로 작성자 점수를 깎지 않는다.
 */
public enum ActivityType {
    /** NEW_FACE — 뉴페이스 */
    PROFILE_SETUP_COMPLETED,
    /** POST_WRITE — 게시글 작성 */
    POST_CREATED,
    /** POST_WRITE — 게시글 삭제 시 회수 */
    POST_DELETED,
    /** COMMENT_WRITE — 댓글 작성 */
    COMMENT_WRITTEN,
    /** COMMENT_WRITE — 댓글 삭제 시 회수 */
    COMMENT_DELETED,
    /** LIKE_RECEIVED — 좋아요 1개 받음, 지급만(취소 시 회수 없음) */
    LIKE_PRESSED,
    /** SCRAP_RECEIVED — 스크랩 1개 받음, 지급만(취소 시 회수 없음) */
    SCRAP_PRESSED;
}
