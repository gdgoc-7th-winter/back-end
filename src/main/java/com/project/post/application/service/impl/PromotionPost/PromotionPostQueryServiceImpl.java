package com.project.post.application.service.impl.PromotionPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PromotionPostQueryService;
import com.project.post.domain.entity.PromotionPost;
import com.project.post.domain.repository.PromotionPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionPostQueryServiceImpl implements PromotionPostQueryService {

    private final PromotionPostRepository promotionPostRepository;
    private final PostQueryService postQueryService;

    @Override
    public PromotionPostDetailResponse getDetail(@NonNull Long postId) {
        PromotionPost promotionPost = promotionPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "홍보글을 찾을 수 없습니다."));

        PostDetailResponse postDetail = postQueryService.getDetail(postId);

        return new PromotionPostDetailResponse(
                promotionPost.getCategory(),
                postDetail
        );
    }
}