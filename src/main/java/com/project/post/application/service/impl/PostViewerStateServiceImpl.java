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
        List<Long> distinctIds = postIds.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        if (viewerUserId == null) {
            PostViewerResponse guest = PostViewerResponse.guest();
            return distinctIds.stream().collect(Collectors.toMap(id -> id, id -> guest));
        }
        Set<Long> idSet = new HashSet<>(distinctIds);
        Set<Long> likedIds = new HashSet<>(postLikeRepository.findPostIdsLikedByUserAndPostIdIn(idSet, viewerUserId));
        Set<Long> scrappedIds = new HashSet<>(postScrapRepository.findPostIdsScrappedByUserAndPostIdIn(idSet, viewerUserId));
        return distinctIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> new PostViewerResponse(
                                likedIds.contains(id),
                                scrappedIds.contains(id),
                                isViewerAuthor(viewerUserId, authorMap.get(id))
                        )
                ));
    }

    private static boolean isViewerAuthor(Long viewerUserId, Long authorUserId) {
        return authorUserId != null && authorUserId.equals(viewerUserId);
    }
}
