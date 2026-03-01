package com.project.user.application.dto.request;

import jakarta.servlet.http.HttpSession;

public record UserPromotionEvent(
        String email,
        HttpSession session,
        Long userId
) {}
