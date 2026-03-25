package com.project.post.application.service.impl.LecturePost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostAuthorResponse;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.dto.LecturePost.LecturePostDetailResponse;
import com.project.post.application.dto.LecturePost.LecturePostListResponse;
import com.project.post.application.service.LecturePostQueryService;
import com.project.post.application.service.PostTagQueryService;
import com.project.post.application.service.PostViewerStateService;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.enums.Campus;
import com.project.post.domain.enums.PostListSort;
import com.project.post.domain.repository.LecturePostRepository;
import com.project.post.domain.repository.dto.LecturePostDetailQueryResult;
import com.project.post.domain.repository.dto.LecturePostListQueryResult;
import com.project.post.domain.repository.dto.LecturePostSearchCondition;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LecturePostQueryServiceImpl implements LecturePostQueryService {

    private final LecturePostRepository lecturePostRepository;
    private final PostTagQueryService postTagQueryService;
    private final PostViewerStateService postViewerStateService;

    @Override
    public Page<LecturePostListResponse> getList(
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            Campus campus,
            List<String> departments,
            String order,
            @Nullable Long viewerUserId) {

        PostListSort sortType = PostListSort.from(order);
        int pageSize = Math.min(pageable.getPageSize(), PostConstants.MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageSize);

        LecturePostSearchCondition condition = new LecturePostSearchCondition(
                keyword, tagNames, campus, departments, sortType
        );

        Page<LecturePostListQueryResult> page = lecturePostRepository.findLecturePostList(safePageable, condition);
        List<Long> postIds = page.getContent().stream().map(LecturePostListQueryResult::postId).toList();
        Map<Long, Long> authorByPostId = page.getContent().stream()
                .collect(Collectors.toMap(LecturePostListQueryResult::postId, LecturePostListQueryResult::authorId));
        var tagNamesByPostId = postTagQueryService.getTagNamesByPostIds(postIds);
        Map<Long, PostViewerResponse> viewerByPostId = postViewerStateService.resolveForPosts(
                viewerUserId, postIds, authorByPostId);

        return page.map(result -> toListResponse(
                result,
                tagNamesByPostId.getOrDefault(result.postId(), List.of()),
                viewerByPostId.getOrDefault(result.postId(), PostViewerResponse.guest())
        ));
    }

    @Override
    public LecturePostDetailResponse getDetail(@NonNull Long postId, @Nullable Long viewerUserId) {
        LecturePostDetailQueryResult result = lecturePostRepository.findLecturePostDetail(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "강의/수업 게시글을 찾을 수 없습니다."));
        PostViewerResponse viewer = postViewerStateService
                .resolveForPosts(viewerUserId, List.of(postId), Map.of(result.postId(), result.authorId()))
                .getOrDefault(postId, PostViewerResponse.guest());
        return toDetailResponse(result, viewer);
    }

    private LecturePostListResponse toListResponse(
            LecturePostListQueryResult result,
            List<String> tagNames,
            PostViewerResponse viewer) {
        return new LecturePostListResponse(
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
                result.department(),
                result.campus(),
                result.viewCount(),
                result.likeCount(),
                result.scrapCount(),
                result.commentCount(),
                viewer,
                tagNames,
                result.createdAt()
        );
    }

    private LecturePostDetailResponse toDetailResponse(LecturePostDetailQueryResult result, PostViewerResponse viewer) {
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
                PostAuthorResponse.fromParts(
                        result.authorId(),
                        result.authorNickname(),
                        result.authorProfileImgUrl(),
                        result.authorDepartmentName(),
                        result.authorRepresentativeTrackName(),
                        result.authorTierBadgeImageUrl()),
                result.department(),
                result.campus(),
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
