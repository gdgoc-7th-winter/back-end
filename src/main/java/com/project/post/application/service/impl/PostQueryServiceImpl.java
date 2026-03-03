package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.domain.entity.PostTag;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.PostTagRepository;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.enums.PostListSort;
import com.project.post.domain.repository.dto.PostSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> getList(
            @NonNull String boardCode,
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            String order) {
        boardRepository.findByCodeAndActiveTrue(boardCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시판을 찾을 수 없습니다."));

        PostListSort sortType = PostListSort.from(order);
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageSize);

        PostSearchCondition condition = new PostSearchCondition(
                keyword,
                tagNames,
                sortType
        );

        Page<PostListQueryResult> page = postRepository.findPostList(boardCode, safePageable, condition);
        Map<Long, List<String>> tagNamesByPostId = loadTagNamesByPostIds(page);
        return page.map(result -> toListResponse(
                result,
                tagNamesByPostId.getOrDefault(result.postId(), List.of())
        ));
    }

    private PostListResponse toListResponse(PostListQueryResult result, List<String> tagNames) {
        return new PostListResponse(
                result.postId(),
                result.title(),
                result.thumbnailUrl(),
                result.authorNickname(),
                result.viewCount(),
                result.likeCount(),
                result.scrapCount(),
                result.commentCount(),
                tagNames,
                result.createdAt()
        );
    }

    private Map<Long, List<String>> loadTagNamesByPostIds(Page<PostListQueryResult> page) {
        List<Long> postIds = page.getContent().stream()
                .map(PostListQueryResult::postId)
                .toList();
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

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getDetail(@NonNull Long postId) {
        PostDetailQueryResult result = postRepository.findPostDetail(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return toResponse(result);
    }

    private PostDetailResponse toResponse(PostDetailQueryResult result) {
        List<String> tagList = result.tagNames() == null
                ? List.of()
                : result.tagNames().stream()
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        List<PostDetailResponse.AttachmentResponse> attachmentList = result.attachments() == null
                ? List.of()
                : result.attachments().stream()
                .filter(Objects::nonNull)
                .filter(a -> a.fileUrl() != null)
                .map(a -> new PostDetailResponse.AttachmentResponse(
                        a.fileUrl(),
                        a.fileName(),
                        a.contentType(),
                        a.fileSize(),
                        a.sortOrder() == null ? 0 : a.sortOrder()
                ))
                .toList();

        return new PostDetailResponse(
                result.postId(),
                result.title(),
                result.content(),
                result.thumbnailUrl(),
                result.authorNickname(),
                result.authorId(),
                result.viewCount(),
                result.likeCount(),
                result.scrapCount(),
                result.commentCount(),
                result.createdAt(),
                result.updatedAt(),
                tagList,
                attachmentList
        );
    }
}
