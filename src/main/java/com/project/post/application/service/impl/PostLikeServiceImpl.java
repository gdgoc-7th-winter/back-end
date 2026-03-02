package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.service.PostLikeService;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.PostLikeRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Override
    @Transactional
    public LikeScrapToggleResponse like(@NonNull Long postId, @NonNull User user) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        int inserted = postLikeRepository.insertIfAbsent(postId, user.getId());
        if (inserted > 0) {
            postRepository.incrementLikeCount(postId);
        }
        long count = postRepository.findLikeCountById(postId).orElse(0L);
        return new LikeScrapToggleResponse(true, count);
    }

    @Override
    @Transactional
    public LikeScrapToggleResponse unlike(@NonNull Long postId, @NonNull User user) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        int deleted = postLikeRepository.deleteByPostIdAndUserId(postId, user.getId());
        if (deleted > 0) {
            postRepository.decrementLikeCount(postId);
        }
        long count = postRepository.findLikeCountById(postId).orElse(0L);
        return new LikeScrapToggleResponse(false, count);
    }
}
