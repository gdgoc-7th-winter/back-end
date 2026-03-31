package com.project.contribution.application.service.support;

import com.project.user.domain.entity.User;

import java.util.Optional;

public record GrantAppendResult(Optional<User> skippedWithUser) {

    public static GrantAppendResult skipped(User user) {
        return new GrantAppendResult(Optional.of(user));
    }

    public static GrantAppendResult proceed() {
        return new GrantAppendResult(Optional.empty());
    }

    public boolean isSkip() {
        return skippedWithUser.isPresent();
    }
}
