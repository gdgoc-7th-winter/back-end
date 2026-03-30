package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.mapper.PostAuthorMapper;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PostTagQueryService;
import com.project.post.application.service.PostViewerStateService;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.enums.PostListSort;
import com.project.post.domain.repository.dto.PostSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final PostTagQueryService postTagQueryService;
    private final PostViewerStateService postViewerStateService;

    @Override
    public Page<PostListResponse> getList(
            @NonNull String boardCode,
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            String order,
            @Nullable Long viewerUserId) {
        boardRepository.findByCodeAndActiveTrue(boardCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시판을 찾을 수 없습니다."));

        PostListSort sortType = PostListSort.from(order);
        int pageSize = Math.min(pageable.getPageSize(), PostConstants.MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageSize);

        PostSearchCondition condition = new PostSearchCondition(
                keyword,
                tagNames,
                sortType
        );

        Page<PostListQueryResult> page = postRepository.findPostList(boardCode, safePageable, condition);
        List<Long> postIds = page.getContent().stream().map(PostListQueryResult::postId).toList();
        Map<Long, Long> authorByPostId = page.getContent().stream()
                .collect(Collectors.toMap(PostListQueryResult::postId, PostListQueryResult::authorId));
        var tagNamesByPostId = postTagQueryService.getTagNamesByPostIds(postIds);
        Map<Long, PostViewerResponse> viewerByPostId = postViewerStateService.resolveForPosts(
                viewerUserId, postIds, authorByPostId);
        return page.map(result -> toListResponse(
                result,
                tagNamesByPostId.getOrDefault(result.postId(), List.of()),
                viewerByPostId.getOrDefault(result.postId(), PostViewerResponse.guest())
        ));
    }

    private PostListResponse toListResponse(
            PostListQueryResult result,
            List<String> tagNames,
            PostViewerResponse viewer) {
        return new PostListResponse(
                result.postId(),
                result.title(),
                result.thumbnailUrl(),
                PostAuthorMapper.from(result),
                result.viewCount(),
                result.likeCount(),
                result.scrapCount(),
                result.commentCount(),
                viewer,
                tagNames,
                result.createdAt()
        );
    }

    @Override
    public PostDetailResponse getDetail(@NonNull Long postId, @Nullable Long viewerUserId) {
        PostDetailQueryResult result = postRepository.findPostDetail(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        PostViewerResponse viewer = postViewerStateService
                .resolveForPosts(viewerUserId, List.of(postId), Map.of(result.postId(), result.authorId()))
                .getOrDefault(postId, PostViewerResponse.guest());
        return toResponse(result, viewer);
    }

    private PostDetailResponse toResponse(PostDetailQueryResult result, PostViewerResponse viewer) {
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
                PostAuthorMapper.from(result),
                result.viewCount(),
                result.likeCount(),
                result.scrapCount(),
                result.commentCount(),
                viewer,
                result.createdAt(),
                result.updatedAt(),
                tagList,
                attachmentList
        );
    }
}
