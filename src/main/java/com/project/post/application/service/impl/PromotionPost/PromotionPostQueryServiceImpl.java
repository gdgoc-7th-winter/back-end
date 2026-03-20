package com.project.post.application.service.impl.PromotionPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostListResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PostTagQueryService;
import com.project.post.application.service.PromotionPostQueryService;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PromotionPost;
import com.project.post.domain.enums.PromotionCategory;
import com.project.post.domain.repository.PromotionPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionPostQueryServiceImpl implements PromotionPostQueryService {

    private final PromotionPostRepository promotionPostRepository;
    private final PostQueryService postQueryService;
    private final PostTagQueryService postTagQueryService;

    @Override
    public PromotionPostDetailResponse getDetail(@NonNull Long postId) {
        PromotionPost promotionPost = promotionPostRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "홍보글을 찾을 수 없습니다."
                ));

        PostDetailResponse postDetail = postQueryService.getDetail(postId);

        return new PromotionPostDetailResponse(
                promotionPost.getCategory(),
                postDetail
        );
    }

    @Override
    public Page<PromotionPostListResponse> getList(
            PromotionCategory category,
            Pageable pageable
    ) {
        int pageSize = Math.min(pageable.getPageSize(), PostConstants.MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());

        Page<PromotionPost> promotionPosts = (category == null)
                ? promotionPostRepository.findAllActive(safePageable)
                : promotionPostRepository.findAllActiveByCategory(category, safePageable);

        List<Long> postIds = promotionPosts.getContent().stream()
                .map(promotionPost -> promotionPost.getPost().getId())
                .toList();

        var tagNamesByPostId = postTagQueryService.getTagNamesByPostIds(postIds);

        return promotionPosts.map(promotionPost -> {
            Post post = promotionPost.getPost();

            PostListResponse postList = new PostListResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getThumbnailUrl(),
                    post.getAuthor().getNickname(),
                    post.getViewCount(),
                    post.getLikeCount(),
                    post.getScrapCount(),
                    post.getCommentCount(),
                    tagNamesByPostId.getOrDefault(post.getId(), List.of()),
                    post.getCreatedAt()
            );

            return new PromotionPostListResponse(
                    promotionPost.getCategory(),
                    postList
            );
        });
    }
}
