package com.project.post.presentation.support;

import com.project.user.domain.entity.User;

import java.util.Optional;

public final class ViewerUserId {

    private ViewerUserId() {
    }

    public static Long from(Optional<User> optionalUser) {
        return optionalUser.map(User::getId).orElse(null);
    }
}
