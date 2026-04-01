package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostCommentRequest;
import com.project.post.application.service.PostCommentCommandService;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostComment;
import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.event.ContributionActivityEvent;
import com.project.contribution.application.service.ContributionFacade;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostCommentCommandServiceImpl implements PostCommentCommandService {

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final ContributionFacade contributionFacade;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public Long create(@NonNull Long postId, @NonNull PostCommentRequest request, @NonNull User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        PostComment comment;
        if (request.parentCommentId() == null) {
            comment = PostComment.createRoot(post, user, request.content());
        } else {
            PostComment parent = commentRepository.findActiveById(request.parentCommentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "부모 댓글을 찾을 수 없습니다."));
            comment = PostComment.createReply(post, user, parent, request.content());
        }

        PostComment savedComment = commentRepository.save(Objects.requireNonNull(comment));
        postRepository.incrementCommentCount(postId);
        contributionFacade.applyActivity(ActivityContext.commentWritten(user.getId(), savedComment.getId()));
        return savedComment.getId();
    }

    @Override
    @Transactional
    public void softDelete(@NonNull Long postId, @NonNull Long commentId, @NonNull User user) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        PostComment comment = commentRepository.findActiveById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "댓글이 해당 게시글에 속하지 않습니다.");
        }

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "삭제 권한이 없습니다.");
        }

        long commentAuthorId = comment.getUser().getId();
        long cid = comment.getId();
        comment.softDelete();
        applicationEventPublisher.publishEvent(
                new ContributionActivityEvent(ActivityContext.commentDeleted(commentAuthorId, cid)));
    }
}
