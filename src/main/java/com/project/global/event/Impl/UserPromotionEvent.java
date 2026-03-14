package com.project.global.event.Impl;

import com.project.global.event.ActivityEvent;
import com.project.global.event.ActivityType;

public record UserPromotionEvent(Long userId, Long profileId) implements ActivityEvent {
    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public Long getReferenceId() {
        return profileId;
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.PROFILE_SETUP_COMPLETED;
    }
}
