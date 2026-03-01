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

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public LikeScrapToggleResponse toggle(@NonNull Long postId, @NonNull User user) {
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        var existing = postLikeRepository.findByPostIdAndUserId(postId, user.getId());

        if (existing.isPresent()) {
            postLikeRepository.delete(Objects.requireNonNull(existing.orElseThrow()));
            post.decrementLikeCount();
            return new LikeScrapToggleResponse(false, post.getLikeCount());
        } else {
            postLikeRepository.save(Objects.requireNonNull(PostLike.of(post, user)));
            post.incrementLikeCount();
            return new LikeScrapToggleResponse(true, post.getLikeCount());
        }
    }
}
