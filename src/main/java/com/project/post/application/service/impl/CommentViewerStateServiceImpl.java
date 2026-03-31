package com.project.post.application.service.impl;

import com.project.post.application.dto.CommentViewerResponse;
import com.project.post.application.service.CommentViewerStateService;
import com.project.post.domain.repository.PostCommentLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentViewerStateServiceImpl implements CommentViewerStateService {

    private final PostCommentLikeRepository postCommentLikeRepository;

    @Override
    public Map<Long, CommentViewerResponse> resolveForComments(
            @Nullable Long viewerUserId,
            Collection<Long> commentIds,
            @Nullable Map<Long, Long> authorUserIdByCommentId) {
        Objects.requireNonNull(commentIds, "commentIds");
        LinkedHashSet<Long> idSet = new LinkedHashSet<>(commentIds);
        if (idSet.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> authorMap = authorUserIdByCommentId == null
                ? Collections.emptyMap()
                : authorUserIdByCommentId;
        if (viewerUserId == null) {
            CommentViewerResponse guest = CommentViewerResponse.guest();
            return idSet.stream().collect(Collectors.toMap(id -> id, id -> guest));
        }
        Set<Long> likedIds = new HashSet<>(
                postCommentLikeRepository.findCommentIdsLikedByUserAndCommentIdIn(idSet, viewerUserId));
        return idSet.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> viewerResponseForLoggedIn(viewerUserId, id, likedIds, authorMap)));
    }

    private static CommentViewerResponse viewerResponseForLoggedIn(
            Long viewerUserId,
            Long commentId,
            Set<Long> likedIds,
            Map<Long, Long> authorUserIdByCommentId) {
        return new CommentViewerResponse(
                likedIds.contains(commentId),
                isViewerAuthor(viewerUserId, authorUserIdByCommentId.get(commentId)));
    }

    private static boolean isViewerAuthor(Long viewerUserId, Long authorUserId) {
        return authorUserId != null && authorUserId.equals(viewerUserId);
    }
}
