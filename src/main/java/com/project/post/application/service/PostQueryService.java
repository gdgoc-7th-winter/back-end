package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    @Transactional(readOnly = true)
    public Page<PostListResponse> getList(@NonNull String boardCode, @NonNull Pageable pageable) {
        boardRepository.findByCodeAndActiveTrue(boardCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시판을 찾을 수 없습니다."));

        Pageable safePageable = pageable.getPageSize() > MAX_PAGE_SIZE
                ? PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort())
                : pageable;

        return postRepository.findPostList(boardCode, safePageable)
                .map(this::toListResponse);
    }

    private PostListResponse toListResponse(PostListQueryResult result) {
        return new PostListResponse(
                result.postId(),
                result.title(),
                result.thumbnailUrl(),
                result.authorNickname(),
                result.viewCount(),
                result.likeCount(),
                result.commentCount(),
                result.createdAt()
        );
    }

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
                .collect(Collectors.toList());

        List<PostDetailResponse.AttachmentResponse> attachmentList = result.attachments() == null
                ? List.of()
                : result.attachments().stream()
                .filter(Objects::nonNull)
                .filter(a -> a.fileUrl() != null)
                .sorted(Comparator.comparingInt(a -> a.sortOrder() == null ? 0 : a.sortOrder()))
                .map(a -> new PostDetailResponse.AttachmentResponse(
                        a.fileUrl(),
                        a.fileName(),
                        a.contentType(),
                        a.fileSize(),
                        a.sortOrder() == null ? 0 : a.sortOrder()
                ))
                .collect(Collectors.toList());

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
