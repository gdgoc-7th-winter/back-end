package com.project.post.application.service.impl.LecturePost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.LecturePost.LecturePostBoardMetadataResponse;
import com.project.post.application.dto.LecturePost.LecturePostDetailResponse;
import com.project.post.application.dto.LecturePost.LecturePostListResponse;
import com.project.post.application.service.LecturePostQueryService;
import com.project.post.domain.enums.BoardType;
import com.project.post.domain.enums.Campus;
import com.project.post.domain.enums.PostListSort;
import com.project.post.domain.entity.PostTag;
import com.project.post.domain.repository.LecturePostRepository;
import com.project.post.domain.repository.PostTagRepository;
import com.project.post.domain.repository.dto.LecturePostDetailQueryResult;
import com.project.post.domain.repository.dto.LecturePostListQueryResult;
import com.project.post.domain.repository.dto.LecturePostSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class LecturePostQueryServiceImpl implements LecturePostQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final LecturePostRepository lecturePostRepository;
    private final PostTagRepository postTagRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LecturePostListResponse> getList(
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            Campus campus,
            List<String> departments,
            String order) {

        PostListSort sortType = PostListSort.from(order);
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageSize);

        LecturePostSearchCondition condition = new LecturePostSearchCondition(
                keyword, tagNames, campus, departments, sortType
        );

        Page<LecturePostListQueryResult> page = lecturePostRepository.findLecturePostList(safePageable, condition);
        Map<Long, List<String>> tagNamesByPostId = loadTagNamesByPostIds(page);

        return page.map(result -> toListResponse(
                result,
                tagNamesByPostId.getOrDefault(result.postId(), List.of())
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public LecturePostDetailResponse getDetail(@NonNull Long postId) {
        LecturePostDetailQueryResult result = lecturePostRepository.findLecturePostDetail(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "강의/수업 게시글을 찾을 수 없습니다."));
        return toDetailResponse(result);
    }

    @Override
    public LecturePostBoardMetadataResponse getBoardMetadata() {
        BoardType boardType = BoardType.LECTURE;
        return new LecturePostBoardMetadataResponse(boardType.getCode());
    }

    private LecturePostListResponse toListResponse(LecturePostListQueryResult result, List<String> tagNames) {
        return new LecturePostListResponse(
                result.postId(),
                result.title(),
                result.thumbnailUrl(),
                result.authorNickname(),
                result.department(),
                result.campus(),
                result.viewCount(),
                result.likeCount(),
                result.scrapCount(),
                result.commentCount(),
                tagNames,
                result.createdAt()
        );
    }

    private LecturePostDetailResponse toDetailResponse(LecturePostDetailQueryResult result) {
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

        return new LecturePostDetailResponse(
                result.postId(),
                result.title(),
                result.content(),
                result.thumbnailUrl(),
                result.authorNickname(),
                result.authorId(),
                result.department(),
                result.campus(),
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

    private Map<Long, List<String>> loadTagNamesByPostIds(Page<LecturePostListQueryResult> page) {
        List<Long> postIds = page.getContent().stream()
                .map(LecturePostListQueryResult::postId)
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
}
