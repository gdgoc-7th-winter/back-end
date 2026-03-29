package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.response.CursorPageResponse;
import com.project.post.application.dto.CommentViewerResponse;
import com.project.post.application.dto.PostCommentChildListResponse;
import com.project.post.application.dto.PostCommentResponse;
import com.project.post.application.dto.PostCommentRootListResponse;
import com.project.post.application.service.CommentViewerStateService;
import com.project.post.application.service.PostCommentQueryService;
import com.project.post.application.support.PostCommentCursorCodec;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.dto.ReplyPreviewRow;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 삭제 댓글은 목록에 포함하고, toResponse에서 deleted=true / content·작성자 null로 마스킹.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCommentQueryServiceImpl implements PostCommentQueryService {

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final CommentViewerStateService commentViewerStateService;

    @Override
    public PostCommentRootListResponse getComments(
            @NonNull Long postId,
            @Nullable String cursor,
            int size,
            @Nullable Long viewerUserId) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        long totalCommentCount = post.getCommentCount();

        int sizeCapped = capCursorPageSize(size);
        PostCommentCursorCodec.Cursor decoded = decodeCursor(cursor);

        List<PostComment> batch = commentRepository.findRootCommentsWithCursor(
                postId,
                decoded != null ? decoded.createdAt() : null,
                decoded != null ? decoded.id() : null,
                sizeCapped + 1);

        boolean hasNext = batch.size() > sizeCapped;
        List<PostComment> rootsSlice = hasNext ? batch.subList(0, sizeCapped) : batch;

        Map<Long, List<PostComment>> repliesByParentId = loadReplyPreviewsByParentId(postId, rootsSlice);

        Map<Long, Long> authorByCommentId = buildAuthorByCommentIdForPage(rootsSlice, repliesByParentId);
        LinkedHashSet<Long> commentIdsOnPage = collectCommentIdsForPage(rootsSlice, repliesByParentId);
        Map<Long, CommentViewerResponse> viewerByCommentId = commentViewerStateService.resolveForComments(
                viewerUserId, commentIdsOnPage, authorByCommentId);

        List<PostCommentResponse> comments = rootsSlice.stream()
                .map(root -> {
                    List<PostComment> replies = repliesByParentId.getOrDefault(root.getId(), List.of());
                    boolean hasMoreReplies = replies.size() > PostConstants.REPLY_PREVIEW_LIMIT;
                    List<PostComment> replyList = hasMoreReplies
                            ? replies.subList(0, PostConstants.REPLY_PREVIEW_LIMIT)
                            : replies;
                    return toResponseWithReplies(root, replyList, hasMoreReplies, viewerByCommentId);
                })
                .collect(Collectors.toList());

        String nextCursor = nextCursorFromSlice(rootsSlice, hasNext);

        return new PostCommentRootListResponse(comments, nextCursor, hasNext, totalCommentCount);
    }

    @Override
    public PostCommentChildListResponse getChildComments(
            @NonNull Long postId,
            @NonNull Long parentCommentId,
            @Nullable String cursor,
            int size,
            @Nullable Long viewerUserId) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        PostComment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!Objects.equals(parent.getPost().getId(), postId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "댓글이 해당 게시글에 속하지 않습니다.");
        }
        if (parent.getParentComment() != null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "부모 댓글은 최상위 댓글이어야 답글 목록을 조회할 수 있습니다.");
        }

        int sizeCapped = capCursorPageSize(size);
        PostCommentCursorCodec.Cursor decoded = decodeCursor(cursor);

        List<PostComment> batch = commentRepository.findCommentsByParentWithCursor(
                postId,
                parentCommentId,
                decoded != null ? decoded.createdAt() : null,
                decoded != null ? decoded.id() : null,
                sizeCapped + 1);

        boolean hasNext = batch.size() > sizeCapped;
        List<PostComment> slice = hasNext ? batch.subList(0, sizeCapped) : batch;

        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        Map<Long, Long> authorById = new HashMap<>();
        for (PostComment c : slice) {
            ids.add(c.getId());
            if (!c.isDeleted()) {
                authorById.put(c.getId(), c.getUser().getId());
            }
        }
        Map<Long, CommentViewerResponse> viewerById = commentViewerStateService.resolveForComments(
                viewerUserId, ids, authorById);

        List<PostCommentResponse> comments = slice.stream()
                .map(c -> toResponse(c, parentCommentId, viewerById))
                .collect(Collectors.toList());

        String nextCursor = nextCursorFromSlice(slice, hasNext);

        return PostCommentChildListResponse.from(CursorPageResponse.of(comments, nextCursor, hasNext));
    }

    private static int capCursorPageSize(int size) {
        return Math.min(Math.max(size, 1), PostConstants.MAX_COMMENT_CURSOR_PAGE_SIZE);
    }

    private static PostCommentCursorCodec.Cursor decodeCursor(@Nullable String cursor) {
        return PostCommentCursorCodec.decode(cursor == null || cursor.isBlank() ? null : cursor);
    }

    private static String nextCursorFromSlice(List<PostComment> slice, boolean hasNext) {
        if (!hasNext || slice.isEmpty()) {
            return null;
        }
        PostComment last = slice.get(slice.size() - 1);
        return PostCommentCursorCodec.encode(last.getCreatedAt(), last.getId());
    }

    private static LinkedHashSet<Long> collectCommentIdsForPage(
            List<PostComment> roots, Map<Long, List<PostComment>> repliesByParentId) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        int preview = PostConstants.REPLY_PREVIEW_LIMIT;
        for (PostComment root : roots) {
            ids.add(root.getId());
            List<PostComment> replies = repliesByParentId.getOrDefault(root.getId(), List.of());
            int n = Math.min(replies.size(), preview);
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
        int preview = PostConstants.REPLY_PREVIEW_LIMIT;
        for (PostComment root : roots) {
            if (!root.isDeleted()) {
                map.put(root.getId(), root.getUser().getId());
            }
            List<PostComment> replies = repliesByParentId.getOrDefault(root.getId(), List.of());
            int n = Math.min(replies.size(), preview);
            for (int i = 0; i < n; i++) {
                PostComment reply = replies.get(i);
                if (!reply.isDeleted()) {
                    map.put(reply.getId(), reply.getUser().getId());
                }
            }
        }
        return map;
    }

    /**
     * DB에서 최상위 댓글당 답글 최대 N+1건만 가져온 뒤, 부모 댓글별로 묶는다.
     */
    private Map<Long, List<PostComment>> loadReplyPreviewsByParentId(long postId, List<PostComment> roots) {
        List<Long> rootIds = roots.stream()
                .map(PostComment::getId)
                .collect(Collectors.toList());
        if (rootIds.isEmpty()) {
            return Map.of();
        }

        int limitPlusOne = PostConstants.REPLY_PREVIEW_LIMIT + 1;
        List<ReplyPreviewRow> previewRows = commentRepository.findReplyPreviewRows(postId, rootIds, limitPlusOne);
        if (previewRows.isEmpty()) {
            return Map.of();
        }

        List<Long> orderedIds = previewRows.stream()
                .map(ReplyPreviewRow::commentId)
                .toList();
        if (orderedIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, PostComment> byId = commentRepository.findAllByIdInWithAssociations(orderedIds).stream()
                .collect(Collectors.toMap(PostComment::getId, Function.identity()));

        Map<Long, List<PostComment>> repliesByParentId = new HashMap<>();
        for (ReplyPreviewRow row : previewRows) {
            PostComment reply = byId.get(row.commentId());
            if (reply == null) {
                continue;
            }
            repliesByParentId
                    .computeIfAbsent(row.parentCommentId(), k -> new ArrayList<>())
                    .add(reply);
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
