package com.project.post.application.service;

import com.project.post.application.dto.PostViewerResponse;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

public interface PostViewerStateService {

    Map<Long, PostViewerResponse> resolveForPosts(
            @Nullable Long viewerUserId,
            List<Long> postIds,
            @Nullable Map<Long, Long> authorUserIdByPostId);
}
