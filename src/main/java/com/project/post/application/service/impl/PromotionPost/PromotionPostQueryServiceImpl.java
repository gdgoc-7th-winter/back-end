package com.project.post.application.service.impl.PromotionPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostAuthorResponse;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostListResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PostTagQueryService;
import com.project.post.application.service.PostViewerStateService;
import com.project.post.application.service.PromotionPostQueryService;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.entity.PromotionPost;
import com.project.post.domain.enums.PromotionCategory;
import com.project.post.domain.repository.PromotionPostRepository;
import com.project.post.domain.repository.dto.PromotionPostListQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionPostQueryServiceImpl implements PromotionPostQueryService {

    private final PromotionPostRepository promotionPostRepository;
    private final PostQueryService postQueryService;
    private final PostTagQueryService postTagQueryService;
    private final PostViewerStateService postViewerStateService;

    @Override
    public PromotionPostDetailResponse getDetail(@NonNull Long postId, @Nullable Long viewerUserId) {
        PromotionPost promotionPost = promotionPostRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "홍보글을 찾을 수 없습니다."
                ));

        PostDetailResponse postDetail = postQueryService.getDetail(postId, viewerUserId);

        return new PromotionPostDetailResponse(
                promotionPost.getCategory(),
                postDetail
        );
    }

    @Override
    public Page<PromotionPostListResponse> getList(
            PromotionCategory category,
            Pageable pageable,
            @Nullable Long viewerUserId
    ) {
        int pageSize = Math.min(pageable.getPageSize(), PostConstants.MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());

        Page<PromotionPostListQueryResult> page = promotionPostRepository.findPromotionPostList(category, safePageable);

        List<Long> postIds = page.getContent().stream().map(PromotionPostListQueryResult::postId).toList();
        Map<Long, Long> authorByPostId = page.getContent().stream()
                .collect(Collectors.toMap(PromotionPostListQueryResult::postId, PromotionPostListQueryResult::authorId));
        var tagNamesByPostId = postTagQueryService.getTagNamesByPostIds(postIds);
        Map<Long, PostViewerResponse> viewerByPostId = postViewerStateService.resolveForPosts(
                viewerUserId, postIds, authorByPostId);

        return page.map(result -> new PromotionPostListResponse(
                result.category(),
                toPostListResponse(
                        result,
                        tagNamesByPostId.getOrDefault(result.postId(), List.of()),
                        viewerByPostId.getOrDefault(result.postId(), PostViewerResponse.guest()))
        ));
    }

    private PostListResponse toPostListResponse(
            PromotionPostListQueryResult result,
            List<String> tagNames,
            PostViewerResponse viewer) {
        return new PostListResponse(
                result.postId(),
                result.title(),
                result.thumbnailUrl(),
                PostAuthorResponse.fromParts(
                        result.authorId(),
                        result.authorNickname(),
                        result.authorProfileImgUrl(),
                        result.authorDepartmentName(),
                        result.authorRepresentativeTrackName(),
                        result.authorTierBadgeImageUrl()),
                result.viewCount(),
                result.likeCount(),
                result.scrapCount(),
                result.commentCount(),
                viewer,
                tagNames,
                result.createdAt()
        );
    }
}
