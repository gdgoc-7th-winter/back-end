package com.project.contribution.application.event;

import com.project.contribution.application.dto.ActivityContext;

/**
 * 게시글 삭제·좋아요 취소·댓글 삭제 등 사용자 주요 동작이 점수 회수 실패로 롤백되지 않게 한다.
 */
public record ContributionActivityEvent(ActivityContext context) {
}
