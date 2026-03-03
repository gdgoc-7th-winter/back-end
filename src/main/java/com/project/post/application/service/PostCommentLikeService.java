package com.project.post.application.service;

import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;

public interface PostCommentLikeService {

    LikeScrapToggleResponse like(@NonNull Long postId, @NonNull Long commentId, @NonNull User user);

    LikeScrapToggleResponse unlike(@NonNull Long postId, @NonNull Long commentId, @NonNull User user);
}
