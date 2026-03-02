package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostLike;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.PostLikeRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public LikeScrapToggleResponse like(@NonNull Long postId, @NonNull User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        var existing = postLikeRepository.findByPostIdAndUserId(postId, user.getId());
        if (existing.isPresent()) {
            int count = postRepository.findLikeCountById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
            return new LikeScrapToggleResponse(true, count);
        }

        PostLike newLike = PostLike.of(post, user);
        postLikeRepository.save(newLike);
        postRepository.incrementLikeCount(postId);
        int count = postRepository.findLikeCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(true, count);
    }

    @Transactional
    public LikeScrapToggleResponse unlike(@NonNull Long postId, @NonNull User user) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        var existing = postLikeRepository.findByPostIdAndUserId(postId, user.getId());
        if (existing.isEmpty()) {
            int count = postRepository.findLikeCountById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
            return new LikeScrapToggleResponse(false, count);
        }

        PostLike existingLike = existing.orElseThrow();
        postLikeRepository.delete(existingLike);
        postRepository.decrementLikeCount(postId);
        int count = postRepository.findLikeCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(false, count);
    }
}
