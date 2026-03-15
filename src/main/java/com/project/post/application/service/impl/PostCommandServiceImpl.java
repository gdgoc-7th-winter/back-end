package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PostUpdateRequest;
import com.project.post.application.service.PostAttachmentService;
import com.project.post.application.service.PostCommandService;
import com.project.post.application.service.PostTagService;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.exception.PostDomainException;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommandServiceImpl implements PostCommandService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final PostTagService postTagService;
    private final PostAttachmentService postAttachmentService;

    @Override
    @Transactional
    public Long create(@NonNull String boardCode, @NonNull PostCreateRequest request, @NonNull User author) {
        Board board = boardRepository.findByCodeAndActiveTrue(boardCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시판을 찾을 수 없습니다."));

        Post post = Post.builder()
                .board(board)
                .author(author)
                .title(request.title())
                .content(request.content())
                .thumbnailUrl(request.thumbnailUrl())
                .build();

        Post savedPost = postRepository.save(post);
        postTagService.replaceTags(savedPost, request.tagNames());
        postAttachmentService.replaceAttachments(savedPost, request.attachments());
        postRepository.flush();

        return savedPost.getId();
    }

    @Override
    @Transactional
    public void update(@NonNull Long postId, @NonNull PostUpdateRequest request, @NonNull User author) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "수정 권한이 없습니다.");
        }

        try {
            post.update(request.title(), request.content(), request.thumbnailUrl());
        } catch (PostDomainException ex) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, ex.getMessage());
        }

        postTagService.replaceTags(post, request.tagNames());
        postAttachmentService.replaceAttachments(post, request.attachments());
        postRepository.flush();
    }

    @Override
    @Transactional
    public void softDelete(@NonNull Long postId, @NonNull User author) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "삭제 권한이 없습니다.");
        }

        post.softDelete();
    }

    @Override
    @Transactional
    public void increaseViewCount(@NonNull Long postId) {
        int updated = postRepository.incrementViewCount(postId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
    }
}
