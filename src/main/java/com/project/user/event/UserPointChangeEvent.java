package com.project.user.event;

public record UserPointChangeEvent(Long userId, Integer newTotalPoint) {
}
