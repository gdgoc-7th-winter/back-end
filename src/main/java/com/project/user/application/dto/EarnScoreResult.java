package com.project.user.application.dto;

import com.project.user.domain.entity.User;

public record EarnScoreResult(User user, boolean grantedNewLedger) {
}
