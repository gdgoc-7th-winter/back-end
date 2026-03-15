package com.project.global.event.Impl;

import com.project.global.event.ActivityEvent;
import com.project.global.event.ActivityType;

public record CommentActivityEvent(Long userId, Long commentId) implements ActivityEvent {
    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public Long getReferenceId() {
        return commentId;
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.COMMENT_WRITTEN;
    }
}
