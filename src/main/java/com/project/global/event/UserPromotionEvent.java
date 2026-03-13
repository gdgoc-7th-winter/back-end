package com.project.global.event;

import jakarta.servlet.http.HttpSession;

public record UserPromotionEvent(
        Long userId,
        HttpSession session
) {}
