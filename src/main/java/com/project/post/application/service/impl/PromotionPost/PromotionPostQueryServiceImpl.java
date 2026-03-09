package com.project.post.application.service.impl.PromotionPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostListResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PromotionPostQueryService;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostTag;
import com.project.post.domain.entity.PromotionPost;
import com.project.post.domain.enums.PromotionCategory;
import com.project.post.domain.repository.PostTagRepository;
import com.project.post.domain.repository.PromotionPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionPostQueryServiceImpl implements PromotionPostQueryService {

    private final PromotionPostRepository promotionPostRepository;
    private final PostQueryService postQueryService;
    private final PostTagRepository postTagRepository;

    @Override
    public PromotionPostDetailResponse getDetail(@NonNull Long postId) {
        PromotionPost promotionPost = promotionPostRepository.findById(postId)
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
        Page<PromotionPost> promotionPosts = (category == null)
                ? promotionPostRepository.findAll(pageable)
                : promotionPostRepository.findByCategory(category, pageable);

        List<Long> postIds = promotionPosts.getContent().stream()
                .map(promotionPost -> promotionPost.getPost().getId())
                .toList();

        Map<Long, List<String>> tagNamesByPostId = loadTagNamesByPostIds(postIds);

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

    private Map<Long, List<String>> loadTagNamesByPostIds(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        List<PostTag> postTags = postTagRepository.findByPostIdIn(postIds);
        Map<Long, List<String>> tagsByPostId = new HashMap<>();

        for (PostTag postTag : postTags) {
            Long postId = postTag.getPost().getId();
            String tagName = postTag.getTag() == null ? null : postTag.getTag().getName();

            if (tagName == null) {
                continue;
            }

            tagsByPostId
                    .computeIfAbsent(postId, id -> new ArrayList<>())
                    .add(tagName);
        }

        tagsByPostId.replaceAll((id, names) -> names.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList());

        return tagsByPostId;
    }
}