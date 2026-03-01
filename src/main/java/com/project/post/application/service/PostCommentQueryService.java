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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentQueryService {

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Page<PostCommentResponse> getComments(@NonNull Long postId, @NonNull Pageable pageable) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        Page<PostComment> rootComments = commentRepository.findRootComments(postId, pageable);
        List<Long> rootIds = rootComments.getContent().stream()
                .map(PostComment::getId)
                .toList();

        List<PostComment> replies = commentRepository.findRepliesByParentIds(Objects.requireNonNull(rootIds));
        Map<Long, List<PostComment>> repliesByParent = replies.stream()
                .filter(c -> c.getParentComment() != null)
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        return rootComments.map(root -> {
            List<PostComment> replyList = repliesByParent.getOrDefault(root.getId(), List.of());
            return toResponseWithReplies(root, replyList);
        });
    }

    private PostCommentResponse toResponse(PostComment comment, Long parentId) {
        return new PostCommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                parentId,
                comment.getDepth(),
                comment.getContent(),
                comment.getLikeCount(),
                comment.getCreatedAt(),
                List.of()
        );
    }

    private PostCommentResponse toResponseWithReplies(PostComment root, List<PostComment> replies) {
        List<PostCommentResponse> replyList = replies.stream()
                .map(reply -> toResponse(reply, root.getId()))
                .collect(Collectors.toList());

        return new PostCommentResponse(
                root.getId(),
                root.getPost().getId(),
                root.getUser().getId(),
                root.getUser().getNickname(),
                null,
                root.getDepth(),
                root.getContent(),
                root.getLikeCount(),
                root.getCreatedAt(),
                replyList
        );
    }
}
