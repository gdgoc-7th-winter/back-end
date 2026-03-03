package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.service.PostCommentLikeService;
import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.PostCommentLikeRepository;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommentLikeServiceImpl implements PostCommentLikeService {

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final PostCommentLikeRepository commentLikeRepository;

    @Override
    @Transactional
    public LikeScrapToggleResponse like(@NonNull Long postId, @NonNull Long commentId, @NonNull User user) {
        validatePostExists(postId);

        PostComment comment = findActiveComment(commentId, postId);

        int inserted = commentLikeRepository.insertIfAbsent(comment.getId(), user.getId());
        if (inserted > 0) {
            commentRepository.incrementLikeCount(comment.getId());
        }
        long count = commentRepository.findLikeCountById(comment.getId()).orElse(0L);
        return new LikeScrapToggleResponse(true, count);
    }

    @Override
    @Transactional
    public LikeScrapToggleResponse unlike(@NonNull Long postId, @NonNull Long commentId, @NonNull User user) {
        validatePostExists(postId);

        PostComment comment = findActiveComment(commentId, postId);

        int deleted = commentLikeRepository.deleteByCommentIdAndUserId(comment.getId(), user.getId());
        if (deleted > 0) {
            commentRepository.decrementLikeCount(comment.getId());
        }
        long count = commentRepository.findLikeCountById(comment.getId()).orElse(0L);
        return new LikeScrapToggleResponse(false, count);
    }

    private void validatePostExists(Long postId) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
    }

    private PostComment findActiveComment(Long commentId, Long postId) {
        PostComment comment = commentRepository.findActiveById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "댓글이 해당 게시글에 속하지 않습니다.");
        }
        return comment;
    }
}
