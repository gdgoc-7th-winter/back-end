package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostScrap;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.PostScrapRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostScrapService {

    private final PostRepository postRepository;
    private final PostScrapRepository postScrapRepository;

    @Transactional
    public LikeScrapToggleResponse scrap(@NonNull Long postId, @NonNull User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        var existing = postScrapRepository.findByPostIdAndUserId(postId, user.getId());
        if (existing.isPresent()) {
            int count = postRepository.findScrapCountById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
            return new LikeScrapToggleResponse(true, count);
        }

        PostScrap newScrap = PostScrap.of(post, user);
        postScrapRepository.save(newScrap);
        postRepository.incrementScrapCount(postId);
        int count = postRepository.findScrapCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(true, count);
    }

    @Transactional
    public LikeScrapToggleResponse unscrap(@NonNull Long postId, @NonNull User user) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        var existing = postScrapRepository.findByPostIdAndUserId(postId, user.getId());
        if (existing.isEmpty()) {
            int count = postRepository.findScrapCountById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
            return new LikeScrapToggleResponse(false, count);
        }

        PostScrap existingScrap = existing.orElseThrow();
        postScrapRepository.delete(existingScrap);
        postRepository.decrementScrapCount(postId);
        int count = postRepository.findScrapCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(false, count);
    }
}
