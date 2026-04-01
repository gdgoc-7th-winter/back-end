package com.project.contribution.application.service.support;

import com.project.user.domain.entity.User;

import java.util.Optional;

public record RevokeAppendResult(Optional<User> skippedWithUser, int actualRevokeMagnitude) {

    public static RevokeAppendResult skipped(User user) {
        return new RevokeAppendResult(Optional.of(user), 0);
    }

    public static RevokeAppendResult proceed(int actualRevokeMagnitude) {
        return new RevokeAppendResult(Optional.empty(), actualRevokeMagnitude);
    }

    public boolean isSkip() {
        return skippedWithUser.isPresent();
    }
}
