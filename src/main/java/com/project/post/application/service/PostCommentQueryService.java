package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostCommentResponse;
import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentQueryService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_REPLIES_PER_ROOT = 20;

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Page<PostCommentResponse> getComments(@NonNull Long postId, @NonNull Pageable pageable) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        Pageable safePageable = pageable.getPageSize() > MAX_PAGE_SIZE
                ? PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort())
                : pageable;

        Page<PostComment> rootComments = commentRepository.findRootComments(postId, safePageable);

        return rootComments.map(root -> {
            List<PostComment> replies = commentRepository.findRepliesByParentId(
                    root.getId(),
                    MAX_REPLIES_PER_ROOT + 1
            );
            boolean hasMoreReplies = replies.size() > MAX_REPLIES_PER_ROOT;
            List<PostComment> replyList = hasMoreReplies
                    ? replies.subList(0, MAX_REPLIES_PER_ROOT)
                    : replies;
            return toResponseWithReplies(root, replyList, hasMoreReplies);
        });
    }

    private PostCommentResponse toResponse(PostComment comment, Long parentId) {
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
                List.of(),
                false
        );
    }

    private PostCommentResponse toResponseWithReplies(PostComment root, List<PostComment> replies, boolean hasMoreReplies) {
        List<PostCommentResponse> replyList = replies.stream()
                .map(reply -> toResponse(reply, root.getId()))
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
                replyList,
                hasMoreReplies
        );
    }
}
