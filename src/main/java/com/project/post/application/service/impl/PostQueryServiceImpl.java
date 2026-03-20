package com.project.post.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PostTagQueryService;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.repository.BoardRepository;
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

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final PostTagQueryService postTagQueryService;

    @Override
    public Page<PostListResponse> getList(
            @NonNull String boardCode,
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            String order) {
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
        var tagNamesByPostId = postTagQueryService.getTagNamesByPostIds(
                page.getContent().stream().map(PostListQueryResult::postId).toList());
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

    @Override
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
