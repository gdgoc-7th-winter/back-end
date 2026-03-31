package com.project.post.application.service.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.service.ContributionFacade;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.service.PostLikeService;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostLike;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.PostLikeRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final ContributionFacade contributionFacade;

    @Override
    @Transactional
    public LikeScrapToggleResponse like(@NonNull Long postId, @NonNull User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        Long authorId = post.getAuthor().getId();

        int inserted = postLikeRepository.insertIfAbsent(postId, user.getId());
        if (inserted > 0) {
            int updated = postRepository.incrementLikeCount(postId);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            }
            postLikeRepository.findByPostIdAndUserId(postId, user.getId())
                    .ifPresent(like -> contributionFacade.applyActivity(
                            ActivityContext.likeReceived(authorId, like.getId(), user.getId())));
        }
        long count = postRepository.findLikeCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(true, count);
    }

    @Override
    @Transactional
    public LikeScrapToggleResponse unlike(@NonNull Long postId, @NonNull User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        Long authorId = post.getAuthor().getId();

        Optional<PostLike> likeOpt = postLikeRepository.findByPostIdAndUserId(postId, user.getId());
        if (likeOpt.isEmpty()) {
            long count = postRepository.findLikeCountById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
            return new LikeScrapToggleResponse(false, count);
        }

        PostLike like = likeOpt.get();
        contributionFacade.applyActivity(ActivityContext.likeCancelled(authorId, like.getId(), user.getId()));

        int deleted = postLikeRepository.deleteByPostIdAndUserId(postId, user.getId());
        if (deleted > 0) {
            int updated = postRepository.decrementLikeCount(postId);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            }
        }
        long count = postRepository.findLikeCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(false, count);
    }
}
