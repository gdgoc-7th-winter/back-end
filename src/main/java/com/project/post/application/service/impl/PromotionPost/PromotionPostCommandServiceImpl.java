package com.project.post.application.service.impl.PromotionPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PromotionPost.PromotionPostCreateRequest;
import com.project.post.application.dto.PromotionPost.PromotionPostUpdateRequest;
import com.project.post.application.service.PostCommandService;
import com.project.post.application.service.PromotionPostCommandService;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PromotionPost;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.PromotionPostRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionPostCommandServiceImpl implements PromotionPostCommandService {

    private static final String PROMOTION_BOARD_CODE = "PROMOTION";

    private final PostCommandService postCommandService;
    private final PostRepository postRepository;
    private final PromotionPostRepository promotionPostRepository;

    @Override
    @Transactional
    public Long create(@NonNull PromotionPostCreateRequest request, @NonNull User author) {
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                request.post().title(),
                request.post().content(),
                request.post().thumbnailUrl(),
                request.post().tagNames(),
                request.post().attachments()
        );

        Long postId = postCommandService.create(PROMOTION_BOARD_CODE, postCreateRequest, author);

        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        PromotionPost promotionPost = PromotionPost.builder()
                .post(post)
                .category(request.category())
                .build();

        promotionPostRepository.save(promotionPost);

        return postId;
    }

    @Override
    @Transactional
    public void update(@NonNull Long postId, @NonNull PromotionPostUpdateRequest request, @NonNull User author) {
        PromotionPost promotionPost = promotionPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "홍보글을 찾을 수 없습니다."));

        if (request.post() != null) {
            postCommandService.update(postId, request.post(), author);
        }

        promotionPost.updateCategory(request.category());
        promotionPostRepository.save(promotionPost);
    }

    @Override
    @Transactional
    public void delete(@NonNull Long postId, @NonNull User author) {
        PromotionPost promotionPost = promotionPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "홍보글을 찾을 수 없습니다."));

        promotionPost.softDelete();
        postCommandService.softDelete(promotionPost.getPost().getId(), author);
    }
}
