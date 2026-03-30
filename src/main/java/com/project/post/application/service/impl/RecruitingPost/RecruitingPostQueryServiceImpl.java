package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostAuthorResponse;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.dto.RecruitingPost.MyRecruitingPostListResponse;
import com.project.post.application.dto.RecruitingPost.MyRecruitingPostSummaryResponse;
import com.project.post.application.dto.RecruitingPost.RecruitingPostDetailResponse;
import com.project.post.application.dto.RecruitingPost.RecruitingPostListResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PostTagQueryService;
import com.project.post.application.service.PostViewerStateService;
import com.project.post.application.service.RecruitingPostQueryService;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.enums.RecruitingStatus;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.post.domain.repository.dto.RecruitingPostListQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitingPostQueryServiceImpl implements RecruitingPostQueryService {

    private final RecruitingPostRepository recruitingPostRepository;
    private final PostQueryService postQueryService;
    private final PostTagQueryService postTagQueryService;
    private final PostViewerStateService postViewerStateService;

    @Override
    public RecruitingPostDetailResponse getDetail(Long postId, Long userId) {
        RecruitingPost recruitingPost = recruitingPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "모집글을 찾을 수 없습니다."
                ));

        PostDetailResponse postDetail = postQueryService.getDetail(postId, userId);

        return new RecruitingPostDetailResponse(
                recruitingPost.getCategory(),
                recruitingPost.getApplicationType(),
                calculateStatus(recruitingPost.getStartedAt(), recruitingPost.getDeadlineAt()),
                calculateStatusLabel(recruitingPost.getStartedAt(), recruitingPost.getDeadlineAt()), // ⭐ 추가
                recruitingPost.getStartedAt(),
                recruitingPost.getDeadlineAt(),
                postDetail
        );
    }

    @Override
    public Page<RecruitingPostListResponse> getList(
            @Nullable RecruitingCategory category,
            Pageable pageable,
            @Nullable Long viewerUserId
    ) {
        int pageSize = Math.min(pageable.getPageSize(), PostConstants.MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());

        Page<RecruitingPostListQueryResult> page =
                recruitingPostRepository.findRecruitingPostList(category, safePageable);

        List<Long> postIds = page.getContent().stream()
                .map(RecruitingPostListQueryResult::postId)
                .toList();

        Map<Long, Long> authorByPostId = page.getContent().stream()
                .collect(Collectors.toMap(
                        RecruitingPostListQueryResult::postId,
                        RecruitingPostListQueryResult::authorId
                ));

        var tagNamesByPostId = postTagQueryService.getTagNamesByPostIds(postIds);

        Map<Long, PostViewerResponse> viewerByPostId =
                postViewerStateService.resolveForPosts(viewerUserId, postIds, authorByPostId);

        return page.map(result -> {
            RecruitingStatus status = calculateStatus(result.startedAt(), result.deadlineAt());

            return new RecruitingPostListResponse(
                    result.category(),
                    result.applicationType(),
                    status,
                    calculateStatusLabel(result.startedAt(), result.deadlineAt()),
                    result.startedAt(),
                    result.deadlineAt(),
                    toPostListResponse(
                            result,
                            tagNamesByPostId.getOrDefault(result.postId(), List.of()),
                            viewerByPostId.getOrDefault(result.postId(), PostViewerResponse.guest())
                    )
            );
        });
    }

    @Override
    public MyRecruitingPostListResponse getMyRecruitingPosts(Long userId) {
        return new MyRecruitingPostListResponse(
                recruitingPostRepository
                        .findAllByPostAuthorIdAndDeletedAtIsNullAndPostDeletedAtIsNullOrderByCreatedAtDesc(userId)
                        .stream()
                        .map(MyRecruitingPostSummaryResponse::from)
                        .toList()
        );
    }

    private PostListResponse toPostListResponse(
            RecruitingPostListQueryResult result,
            List<String> tagNames,
            PostViewerResponse viewer
    ) {
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
                        result.authorLevelImageUrl()
                ),
                result.viewCount(),
                result.likeCount(),
                result.scrapCount(),
                result.commentCount(),
                viewer,
                tagNames,
                result.createdAt()
        );
    }

    private RecruitingStatus calculateStatus(Instant startedAt, Instant deadlineAt) {
        Instant now = Instant.now();

        if (startedAt != null && now.isBefore(startedAt)) {
            return RecruitingStatus.UPCOMING;
        }

        if (deadlineAt != null && now.isAfter(deadlineAt)) {
            return RecruitingStatus.CLOSED;
        }

        return RecruitingStatus.OPEN;
    }

    private String calculateStatusLabel(Instant startedAt, Instant deadlineAt) {
        Instant now = Instant.now();

        if (startedAt != null && now.isBefore(startedAt)) {
            return "모집 예정";
        }

        if (deadlineAt != null && now.isAfter(deadlineAt)) {
            return "모집 마감";
        }

        if (deadlineAt == null) {
            return "모집 중";
        }

        LocalDate today = now.atZone(ZoneId.of("Asia/Seoul")).toLocalDate();
        LocalDate deadlineDate = deadlineAt.atZone(ZoneId.of("Asia/Seoul")).toLocalDate();

        long days = ChronoUnit.DAYS.between(today, deadlineDate);

        if (days <= 0) {
            return "D-Day";
        }

        return "D-" + days;
    }
}