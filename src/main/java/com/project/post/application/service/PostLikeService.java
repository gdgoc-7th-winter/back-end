package com.project.post.application.service;

import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;
public interface PostLikeService {

    LikeScrapToggleResponse like(@NonNull Long postId, @NonNull User user);

    LikeScrapToggleResponse unlike(@NonNull Long postId, @NonNull User user);
}
