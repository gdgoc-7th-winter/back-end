package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LecturePost.LecturePostDetailResponse;
import com.project.post.application.dto.LecturePost.LecturePostListResponse;
import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.service.impl.LecturePost.LecturePostQueryServiceImpl;
import com.project.post.domain.enums.Campus;
import com.project.post.domain.repository.LecturePostRepository;
import com.project.post.domain.repository.dto.LecturePostDetailQueryResult;
import com.project.post.domain.repository.dto.LecturePostListQueryResult;
import com.project.post.domain.repository.dto.LecturePostSearchCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LecturePostQueryServiceTest {

    @Mock
    private LecturePostRepository lecturePostRepository;

    @Mock
    private PostTagQueryService postTagQueryService;

    @Mock
    private PostViewerStateService postViewerStateService;

    @InjectMocks
    private LecturePostQueryServiceImpl lecturePostQueryService;

    @Test
    @DisplayName("강의/수업 게시글 목록을 페이징하여 조회한다")
    void getListReturnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        LecturePostListQueryResult result = new LecturePostListQueryResult(
                1L, "알고리즘 과제", "본문입니다", null,
                1L, "테스터", null, "영어통번역학과", "백엔드", null,
                false,
                "컴퓨터공학과", Campus.SEOUL,
                10, 5, 3, 2, Instant.now()
        );

        Page<LecturePostListQueryResult> queryPage = new PageImpl<>(List.of(result), pageable, 1);
        when(lecturePostRepository.findLecturePostList(any(Pageable.class), any(LecturePostSearchCondition.class)))
                .thenReturn(queryPage);
        when(postTagQueryService.getTagNamesByPostIds(List.of(1L))).thenReturn(Map.of());
        when(postViewerStateService.resolveForPosts(isNull(), eq(List.of(1L)), eq(Map.of(1L, 1L)))).thenReturn(
                Map.of(1L, PostViewerResponse.guest()));

        Page<LecturePostListResponse> response = lecturePostQueryService.getList(
                pageable, null, null, null, null, "latest", null);

        assertThat(response.getContent()).hasSize(1);
        LecturePostListResponse item = response.getContent().get(0);
        assertThat(item.postId()).isEqualTo(1L);
        assertThat(item.department()).isEqualTo("컴퓨터공학과");
        assertThat(item.campus()).isEqualTo(Campus.SEOUL);
    }

    @Test
    @DisplayName("캠퍼스 필터가 검색 조건에 전달된다")
    void getListPassesCampusFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<LecturePostListQueryResult> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(lecturePostRepository.findLecturePostList(any(Pageable.class), any(LecturePostSearchCondition.class)))
                .thenReturn(emptyPage);
        when(postTagQueryService.getTagNamesByPostIds(List.of())).thenReturn(Map.of());
        when(postViewerStateService.resolveForPosts(isNull(), eq(List.of()), eq(Map.of()))).thenReturn(Map.of());

        lecturePostQueryService.getList(pageable, null, null, Campus.GLOBAL, null, "latest", null);

        ArgumentCaptor<LecturePostSearchCondition> condCaptor = ArgumentCaptor.forClass(LecturePostSearchCondition.class);
        org.mockito.Mockito.verify(lecturePostRepository).findLecturePostList(any(), condCaptor.capture());
        assertThat(condCaptor.getValue().campus()).isEqualTo(Campus.GLOBAL);
    }

    @Test
    @DisplayName("학과 다중 필터가 검색 조건에 전달된다")
    void getListPassesDepartmentFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<LecturePostListQueryResult> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(lecturePostRepository.findLecturePostList(any(Pageable.class), any(LecturePostSearchCondition.class)))
                .thenReturn(emptyPage);
        when(postTagQueryService.getTagNamesByPostIds(List.of())).thenReturn(Map.of());
        when(postViewerStateService.resolveForPosts(isNull(), eq(List.of()), eq(Map.of()))).thenReturn(Map.of());

        lecturePostQueryService.getList(pageable, null, null, null,
                List.of("컴퓨터공학과", "경영학과"), "latest", null);

        ArgumentCaptor<LecturePostSearchCondition> condCaptor = ArgumentCaptor.forClass(LecturePostSearchCondition.class);
        org.mockito.Mockito.verify(lecturePostRepository).findLecturePostList(any(), condCaptor.capture());
        assertThat(condCaptor.getValue().departments()).containsExactly("컴퓨터공학과", "경영학과");
    }

    @Test
    @DisplayName("삭제된/없는 강의 게시글은 상세 조회 시 RESOURCE_NOT_FOUND (findLecturePostDetail은 deleted_at 제외)")
    void getDetailThrowsWhenMissingOrDeleted() {
        when(lecturePostRepository.findLecturePostDetail(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lecturePostQueryService.getDetail(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("강의/수업 게시글 상세를 조회한다")
    void getDetailReturnsResponse() {
        LecturePostDetailQueryResult result = new LecturePostDetailQueryResult(
                1L, "제목", "본문", null,
                10L, "닉네임", null, null, null, null,
                false,
                "컴퓨터공학과", Campus.SEOUL,
                100, 50, 30, 20,
                Instant.now(), Instant.now(),
                List.of("Java", "Spring"),
                List.of()
        );

        when(lecturePostRepository.findLecturePostDetail(1L)).thenReturn(Optional.of(result));
        when(postViewerStateService.resolveForPosts(isNull(), eq(List.of(1L)), eq(Map.of(1L, 10L)))).thenReturn(
                Map.of(1L, PostViewerResponse.guest()));

        LecturePostDetailResponse response = lecturePostQueryService.getDetail(1L, null);

        assertThat(response.postId()).isEqualTo(1L);
        assertThat(response.department()).isEqualTo("컴퓨터공학과");
        assertThat(response.campus()).isEqualTo(Campus.SEOUL);
        assertThat(response.tagNames()).containsExactly("Java", "Spring");
    }

    @Test
    @DisplayName("페이지 크기가 최대값을 초과하면 잘린다")
    void getListCapsPageSize() {
        Pageable largePage = PageRequest.of(0, 500);
        Page<LecturePostListQueryResult> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(lecturePostRepository.findLecturePostList(any(Pageable.class), any(LecturePostSearchCondition.class)))
                .thenReturn(emptyPage);
        when(postTagQueryService.getTagNamesByPostIds(List.of())).thenReturn(Map.of());
        when(postViewerStateService.resolveForPosts(isNull(), eq(List.of()), eq(Map.of()))).thenReturn(Map.of());

        lecturePostQueryService.getList(largePage, null, null, null, null, "latest", null);

        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(lecturePostRepository).findLecturePostList(pageCaptor.capture(), any());
        assertThat(pageCaptor.getValue().getPageSize()).isEqualTo(100);
    }
}
