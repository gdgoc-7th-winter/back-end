package com.project.post.domain.constants;

public final class PostConstants {

    /** 목록 조회 시 허용 최대 페이지 크기 */
    public static final int MAX_PAGE_SIZE = 100;

    /** 댓글 목록 API에서 최상위 댓글당 보여 줄 답글 미리보기 개수 */
    public static final int REPLY_PREVIEW_LIMIT = 20;

    /** 최상위 댓글 목록·답글 목록 조회 API 1회 최대 size */
    public static final int MAX_COMMENT_CURSOR_PAGE_SIZE = 100;

    private PostConstants() {
    }
}
