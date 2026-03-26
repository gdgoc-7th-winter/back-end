package com.project.post.application.service;

import com.project.post.application.dto.CommentViewerResponse;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;

public interface CommentViewerStateService {

    Map<Long, CommentViewerResponse> resolveForComments(
            @Nullable Long viewerUserId,
            Collection<Long> commentIds,
            @Nullable Map<Long, Long> authorUserIdByCommentId);
}
