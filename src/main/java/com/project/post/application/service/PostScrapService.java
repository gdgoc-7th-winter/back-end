package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
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
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        int inserted = postScrapRepository.insertIfAbsent(postId, user.getId());
        if (inserted > 0) {
            postRepository.incrementScrapCount(postId);
        }
        long count = postRepository.findScrapCountById(postId).orElse(0L);
        return new LikeScrapToggleResponse(true, count);
    }

    @Transactional
    public LikeScrapToggleResponse unscrap(@NonNull Long postId, @NonNull User user) {
        if (!postRepository.existsActiveById(postId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        int deleted = postScrapRepository.deleteByPostIdAndUserId(postId, user.getId());
        if (deleted > 0) {
            postRepository.decrementScrapCount(postId);
        }
        long count = postRepository.findScrapCountById(postId).orElse(0L);
        return new LikeScrapToggleResponse(false, count);
    }
}
