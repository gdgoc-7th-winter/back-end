package com.project.global.event.impl;

import com.project.global.event.ActivityEvent;
import com.project.global.event.ActivityType;

public record PostActivityEvent(Long userId, Long postId) implements ActivityEvent {
    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public Long getReferenceId() {
        return postId;
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.POST_CREATED;
    }
}
