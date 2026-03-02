package com.project.post.application.service;

import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;
public interface PostScrapService {

    LikeScrapToggleResponse scrap(@NonNull Long postId, @NonNull User user);

    LikeScrapToggleResponse unscrap(@NonNull Long postId, @NonNull User user);
}
