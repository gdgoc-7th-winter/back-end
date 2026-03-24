package com.project.post.application.service.impl;

import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.service.PostViewerStateService;
import com.project.post.domain.repository.PostLikeRepository;
import com.project.post.domain.repository.PostScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostViewerStateServiceImpl implements PostViewerStateService {

    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;

    @Override
    public Map<Long, PostViewerResponse> resolveForPosts(
            @Nullable Long viewerUserId,
            List<Long> postIds,
            @Nullable Map<Long, Long> authorUserIdByPostId) {
        Objects.requireNonNull(postIds, "postIds");
        Map<Long, Long> authorMap = authorUserIdByPostId == null
                ? Collections.emptyMap()
                : authorUserIdByPostId;
        LinkedHashSet<Long> postIdSet = new LinkedHashSet<>(postIds);
        if (postIdSet.isEmpty()) {
            return Map.of();
        }
        if (viewerUserId == null) {
            PostViewerResponse guest = PostViewerResponse.guest();
            return postIdSet.stream().collect(Collectors.toMap(id -> id, id -> guest));
        }
        Set<Long> likedIds = new HashSet<>(
                postLikeRepository.findPostIdsLikedByUserAndPostIdIn(postIdSet, viewerUserId));
        Set<Long> scrappedIds = new HashSet<>(
                postScrapRepository.findPostIdsScrappedByUserAndPostIdIn(postIdSet, viewerUserId));
        return postIdSet.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> viewerResponseForLoggedIn(
                                viewerUserId, id, likedIds, scrappedIds, authorMap)));
    }

    private static PostViewerResponse viewerResponseForLoggedIn(
            Long viewerUserId,
            Long postId,
            Set<Long> likedIds,
            Set<Long> scrappedIds,
            Map<Long, Long> authorUserIdByPostId) {
        return new PostViewerResponse(
                likedIds.contains(postId),
                scrappedIds.contains(postId),
                isViewerAuthor(viewerUserId, authorUserIdByPostId.get(postId)));
    }

    private static boolean isViewerAuthor(Long viewerUserId, Long authorUserId) {
        return authorUserId != null && authorUserId.equals(viewerUserId);
    }
}
