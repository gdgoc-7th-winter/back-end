package com.project.global.event;

public interface ActivityEvent {
    Long getUserId();
    Long getReferenceId();
    ActivityType getActivityType();
}
