package com.project.global.event;
import java.time.LocalDateTime;

public record ActivityEvent (
        Long userId,
        ActivityType activityType,
        boolean isSuccess, // 성공 여부 필드
        LocalDateTime timestamp
) {
}
