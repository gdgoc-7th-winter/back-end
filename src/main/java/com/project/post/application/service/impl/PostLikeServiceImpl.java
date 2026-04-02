package com.project.post.application.service.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.port.ContributionOutboxPort;
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
    private final ContributionOutboxPort contributionOutboxPort;

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
            PostLike like = postLikeRepository.findByPostIdAndUserId(postId, user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                            "좋아요 저장 후 조회에 실패했습니다. 잠시 후 다시 시도해 주세요."));
            contributionOutboxPort.append(ActivityContext.likeReceived(authorId, like.getId(), user.getId()));
        }
        long count = postRepository.findLikeCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(true, count);
    }

    @Override
    @Transactional
    public LikeScrapToggleResponse unlike(@NonNull Long postId, @NonNull User user) {
        postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        Optional<PostLike> likeOpt = postLikeRepository.findByPostIdAndUserId(postId, user.getId());
        if (likeOpt.isEmpty()) {
            long count = postRepository.findLikeCountById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
            return new LikeScrapToggleResponse(false, count);
        }

        int deleted = postLikeRepository.deleteByPostIdAndUserId(postId, user.getId());
        if (deleted > 0) {
            int updated = postRepository.decrementLikeCount(postId);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            }
            // 좋아요: 스크랩과 같이 지급만 하며, 취소 시 작성자 점수는 회수하지 않는다.
        }
        long count = postRepository.findLikeCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(false, count);
    }
}
