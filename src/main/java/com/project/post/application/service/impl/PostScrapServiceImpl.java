package com.project.post.application.service.impl;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.port.ContributionOutboxPort;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.service.PostScrapService;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostScrap;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.PostScrapRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostScrapServiceImpl implements PostScrapService {

    private final PostRepository postRepository;
    private final PostScrapRepository postScrapRepository;
    private final ContributionOutboxPort contributionOutboxPort;

    @Override
    @Transactional
    public LikeScrapToggleResponse scrap(@NonNull Long postId, @NonNull User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        Long authorId = post.getAuthor().getId();

        int inserted = postScrapRepository.insertIfAbsent(postId, user.getId());
        if (inserted > 0) {
            int updated = postRepository.incrementScrapCount(postId);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            }
            postScrapRepository.findByPostIdAndUserId(postId, user.getId())
                    .ifPresent(scrap -> contributionOutboxPort.append(
                            ActivityContext.scrapReceived(authorId, scrap.getId(), user.getId())));
        }
        long count = postRepository.findScrapCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(true, count);
    }

    @Override
    @Transactional
    public LikeScrapToggleResponse unscrap(@NonNull Long postId, @NonNull User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        Optional<PostScrap> scrapOpt = postScrapRepository.findByPostIdAndUserId(postId, user.getId());
        if (scrapOpt.isEmpty()) {
            long count = postRepository.findScrapCountById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
            return new LikeScrapToggleResponse(false, count);
        }

        int deleted = postScrapRepository.deleteByPostIdAndUserId(postId, user.getId());
        if (deleted > 0) {
            int updated = postRepository.decrementScrapCount(postId);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            }
        }
        long count = postRepository.findScrapCountById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return new LikeScrapToggleResponse(false, count);
    }
}
