package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.CommentViewerResponse;
import com.project.post.application.dto.PostCommentResponse;
import com.project.post.application.service.CommentViewerStateService;
import com.project.post.application.service.PostCommentQueryService;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 삭제 댓글은 목록에 포함하고, toResponse에서 deleted=true / content·작성자 null로 마스킹.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCommentQueryServiceImpl implements PostCommentQueryService {

    private static final int MAX_REPLIES_PER_ROOT = 20;

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final CommentViewerStateService commentViewerStateService;

    @Override
    public Page<PostCommentResponse> getComments(
            @NonNull Long postId,
            @NonNull Pageable pageable,
            @Nullable Long viewerUserId) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        Pageable safePageable = pageable.getPageSize() > PostConstants.MAX_PAGE_SIZE
                ? PageRequest.of(pageable.getPageNumber(), PostConstants.MAX_PAGE_SIZE, pageable.getSort())
                : pageable;

        Page<PostComment> rootComments = commentRepository.findRootComments(postId, safePageable);
        Map<Long, List<PostComment>> repliesByParentId = loadRepliesByParentId(rootComments);

        List<PostComment> rootsOnPage = rootComments.getContent();
        Map<Long, Long> authorByCommentId = buildAuthorByCommentIdForPage(rootsOnPage, repliesByParentId);
        LinkedHashSet<Long> commentIdsOnPage = collectCommentIdsForPage(rootsOnPage, repliesByParentId);
        Map<Long, CommentViewerResponse> viewerByCommentId = commentViewerStateService.resolveForComments(
                viewerUserId, commentIdsOnPage, authorByCommentId);

        return rootComments.map(root -> {
            List<PostComment> replies = repliesByParentId.getOrDefault(root.getId(), List.of());
            boolean hasMoreReplies = replies.size() > MAX_REPLIES_PER_ROOT;
            List<PostComment> replyList = hasMoreReplies
                    ? replies.subList(0, MAX_REPLIES_PER_ROOT)
                    : replies;
            return toResponseWithReplies(root, replyList, hasMoreReplies, viewerByCommentId);
        });
    }

    private static LinkedHashSet<Long> collectCommentIdsForPage(
            List<PostComment> roots, Map<Long, List<PostComment>> repliesByParentId) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (PostComment root : roots) {
            ids.add(root.getId());
            List<PostComment> replies = repliesByParentId.getOrDefault(root.getId(), List.of());
            int n = Math.min(replies.size(), MAX_REPLIES_PER_ROOT);
            for (int i = 0; i < n; i++) {
                ids.add(replies.get(i).getId());
            }
        }
        return ids;
    }

    /**
     * 삭제된 댓글은 작성자 ID를 넣지 않아 viewer.isAuthor 가 항상 false
     */
    private static Map<Long, Long> buildAuthorByCommentIdForPage(
            List<PostComment> roots, Map<Long, List<PostComment>> repliesByParentId) {
        Map<Long, Long> map = new HashMap<>();
        for (PostComment root : roots) {
            if (!root.isDeleted()) {
                map.put(root.getId(), root.getUser().getId());
            }
            List<PostComment> replies = repliesByParentId.getOrDefault(root.getId(), List.of());
            int n = Math.min(replies.size(), MAX_REPLIES_PER_ROOT);
            for (int i = 0; i < n; i++) {
                PostComment reply = replies.get(i);
                if (!reply.isDeleted()) {
                    map.put(reply.getId(), reply.getUser().getId());
                }
            }
        }
        return map;
    }

    private Map<Long, List<PostComment>> loadRepliesByParentId(Page<PostComment> rootComments) {
        List<Long> rootIds = rootComments.getContent().stream()
                .map(PostComment::getId)
                .collect(Collectors.toList());
        if (rootIds.isEmpty()) {
            return Map.of();
        }

        List<PostComment> replies = commentRepository.findRepliesByParentIds(rootIds);
        Map<Long, List<PostComment>> repliesByParentId = new HashMap<>();
        for (PostComment reply : replies) {
            Long parentId = reply.getParentComment().getId();
            List<PostComment> list = repliesByParentId.computeIfAbsent(parentId, id -> new ArrayList<>());
            if (list.size() < MAX_REPLIES_PER_ROOT + 1) {
                list.add(reply);
            }
        }
        return repliesByParentId;
    }

    /** 삭제 댓글: deleted=true, content·작성자 null */
    private PostCommentResponse toResponse(
            PostComment comment,
            Long parentId,
            Map<Long, CommentViewerResponse> viewerByCommentId) {
        boolean deleted = comment.isDeleted();
        return new PostCommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                deleted ? null : comment.getUser().getId(),
                deleted ? null : comment.getUser().getNickname(),
                parentId,
                comment.getDepth(),
                deleted ? null : comment.getContent(),
                deleted,
                comment.getLikeCount(),
                comment.getCreatedAt(),
                viewerByCommentId.getOrDefault(comment.getId(), CommentViewerResponse.guest()),
                List.of(),
                false
        );
    }

    private PostCommentResponse toResponseWithReplies(
            PostComment root,
            List<PostComment> replies,
            boolean hasMoreReplies,
            Map<Long, CommentViewerResponse> viewerByCommentId) {
        List<PostCommentResponse> replyList = replies.stream()
                .map(reply -> toResponse(reply, root.getId(), viewerByCommentId))
                .collect(Collectors.toList());

        boolean deleted = root.isDeleted();
        return new PostCommentResponse(
                root.getId(),
                root.getPost().getId(),
                deleted ? null : root.getUser().getId(),
                deleted ? null : root.getUser().getNickname(),
                null,
                root.getDepth(),
                deleted ? null : root.getContent(),
                deleted,
                root.getLikeCount(),
                root.getCreatedAt(),
                viewerByCommentId.getOrDefault(root.getId(), CommentViewerResponse.guest()),
                replyList,
                hasMoreReplies
        );
    }
}
