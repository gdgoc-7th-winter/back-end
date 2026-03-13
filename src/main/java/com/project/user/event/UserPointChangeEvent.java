package com.project.user.event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserPointChangeEvent {
    private final Long userId;
    private final Integer currentPoint;
}
