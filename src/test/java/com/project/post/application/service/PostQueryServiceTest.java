package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostAuthorResponse;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.service.impl.PostQueryServiceImpl;
import com.project.post.domain.entity.Board;
import com.project.post.domain.enums.PostListSort;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.repository.dto.PostSearchCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostTagQueryService postTagQueryService;

    @Mock
    private PostViewerStateService postViewerStateService;

    @InjectMocks
    private PostQueryServiceImpl postQueryService;

    @Test
    @DisplayName("게시판이 없으면 목록 조회는 예외를 던진다")
    void getListThrowsWhenBoardMissing() {
        when(boardRepository.findByCodeAndActiveTrue("GENERAL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postQueryService.getList("GENERAL", PageRequest.of(0, 10), null, null, null, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("삭제된 게시글은 상세 조회 시 RESOURCE_NOT_FOUND (findPostDetail은 deleted_at 제외)")
    void getDetailThrowsWhenPostDeletedOrMissing() {
        when(postRepository.findPostDetail(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postQueryService.getDetail(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("상세 조회는 태그/첨부를 정렬 및 필터링한다")
    void getDetailMapsTagsAndAttachments() {
        List<String> tagNames = new ArrayList<>(List.of("spring", "java"));
        tagNames.add(null);

        List<PostDetailQueryResult.AttachmentDto> attachments = new ArrayList<>();
        attachments.add(new PostDetailQueryResult.AttachmentDto(null, "skip", "text/plain", 10L, 1));
        attachments.add(new PostDetailQueryResult.AttachmentDto("url-1", "a.txt", "text/plain", 5L, null));
        attachments.add(new PostDetailQueryResult.AttachmentDto("url-2", "b.txt", "text/plain", 20L, 2));

        PostDetailQueryResult result = new PostDetailQueryResult(
                1L,
                "title",
                "content",
                "thumb",
                10L,
                "author",
                null,
                null,
                null,
                null,
                false,
                3L,
                2L,
                1L,
                4L,
                Instant.now(),
                Instant.now(),
                tagNames,
                attachments
        );

        when(postRepository.findPostDetail(1L)).thenReturn(Optional.of(result));
        when(postViewerStateService.resolveForPosts(isNull(), eq(List.of(1L)), eq(Map.of(1L, 10L)))).thenReturn(
                Map.of(1L, PostViewerResponse.guest()));

        PostDetailResponse response = postQueryService.getDetail(1L, null);

        assertThat(response.tagNames()).containsExactly("java", "spring");
        assertThat(response.attachments()).hasSize(2);
        assertThat(response.attachments().get(0).fileUrl()).isEqualTo("url-1");
        assertThat(response.attachments().get(0).sortOrder()).isEqualTo(0);
        assertThat(response.attachments().get(1).fileUrl()).isEqualTo("url-2");
        assertThat(response.attachments().get(1).sortOrder()).isEqualTo(2);
        assertThat(response.viewer()).isEqualTo(PostViewerResponse.guest());
    }

    @Test
    @DisplayName("목록 조회는 레포지토리 결과를 Response DTO로 변환하여 반환한다")
    void getListReturnsRepositoryPage() {
        when(boardRepository.findByCodeAndActiveTrue("GENERAL")).thenReturn(Optional.of(Board.of("GENERAL", "자유/정보 게시판")));

        Page<PostListQueryResult> queryPage = new PageImpl<>(Objects.requireNonNull(List.of(
                new PostListQueryResult(1L, "t", "본문", "thumb", 1L, "nick", null, null, null, null, false, 0, 0, 0, 0, Instant.now())
        )));
        PostSearchCondition condition = new PostSearchCondition(null, null, PostListSort.LATEST);
        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.findPostList("GENERAL", pageable, condition)).thenReturn(queryPage);
        when(postTagQueryService.getTagNamesByPostIds(List.of(1L))).thenReturn(
                java.util.Map.of(1L, List.of("java")));
        when(postViewerStateService.resolveForPosts(isNull(), eq(List.of(1L)), eq(Map.of(1L, 1L)))).thenReturn(
                Map.of(1L, PostViewerResponse.guest()));

        Page<PostListResponse> result = postQueryService.getList("GENERAL", PageRequest.of(0, 10), null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).postId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).viewer()).isEqualTo(PostViewerResponse.guest());
    }

    @Test
    @DisplayName("전체 게시판 목록은 게시판 검증 없이 활성 게시판 전체 레포지토리를 호출한다")
    void getListAllBoardsCallsRepositoryWithoutBoardLookup() {
        Page<PostListQueryResult> queryPage = new PageImpl<>(Objects.requireNonNull(List.of(
                new PostListQueryResult(1L, "t", "본문","thumb", 1L, "nick", null, null, null, null, false, 0, 0, 0, 0, Instant.now())
        )));
        PostSearchCondition condition = new PostSearchCondition(null, null, PostListSort.LATEST);
        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.findPostListAllActiveBoards(pageable, condition)).thenReturn(queryPage);
        when(postTagQueryService.getTagNamesByPostIds(List.of(1L))).thenReturn(
                java.util.Map.of(1L, List.of("java")));
        when(postViewerStateService.resolveForPosts(isNull(), eq(List.of(1L)), eq(Map.of(1L, 1L)))).thenReturn(
                Map.of(1L, PostViewerResponse.guest()));

        Page<PostListResponse> result = postQueryService.getListAllBoards(PageRequest.of(0, 10), null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).postId()).isEqualTo(1L);
        verifyNoInteractions(boardRepository);
    }

    // ── 탈퇴 사용자가 게시글 작성자인 경우 ──────────────────────────────────────

    /**
     * 탈퇴 사용자가 작성자인 게시글 목록/상세: 레포지토리 프로젝션 결과를 그대로 API로 매핑한다.
     * {@code User.withdraw()} 는 닉네임 등을 비우고 {@code deleted_at} 을 설정하며,
     * 탈퇴 여부는 {@code authorWithdrawn} 과 {@link PostAuthorResponse#isWithdrawn()} 으로 전달된다.
     */
    @Nested
    @DisplayName("탈퇴 사용자가 게시글 작성자인 경우")
    class WithdrawnAuthorTests {

        private static final Long WITHDRAWN_AUTHOR_ID = 99L;

        @Test
        @DisplayName("목록 조회: 탈퇴 작성자는 닉네임 등 null·authorWithdrawn=true 가 매핑된다")
        void getListWithWithdrawnAuthorMapsAnonymizedFields() {
            when(boardRepository.findByCodeAndActiveTrue("GENERAL"))
                    .thenReturn(Optional.of(Board.of("GENERAL", "자유/정보 게시판")));

            // withdraw() 이후 DB/프로젝션에 가깝게: nickname null, authorWithdrawn true
            PostListQueryResult result = new PostListQueryResult(
                    1L, "title", "본문",null,
                    WITHDRAWN_AUTHOR_ID, null, null, null, null, null,
                    true,
                    0, 0, 0, 0, Instant.now()
            );
            Page<PostListQueryResult> queryPage = new PageImpl<>(List.of(result));
            PostSearchCondition condition = new PostSearchCondition(null, null, PostListSort.LATEST);
            Pageable pageable = PageRequest.of(0, 10);

            when(postRepository.findPostList("GENERAL", pageable, condition)).thenReturn(queryPage);
            when(postTagQueryService.getTagNamesByPostIds(List.of(1L))).thenReturn(Map.of());
            when(postViewerStateService.resolveForPosts(
                    isNull(), eq(List.of(1L)), eq(Map.of(1L, WITHDRAWN_AUTHOR_ID))))
                    .thenReturn(Map.of(1L, PostViewerResponse.guest()));

            Page<PostListResponse> page = postQueryService.getList(
                    "GENERAL", PageRequest.of(0, 10), null, null, null, null);

            assertThat(page.getContent()).hasSize(1);
            PostAuthorResponse author = page.getContent().get(0).author();
            assertThat(author.authorId()).isEqualTo(WITHDRAWN_AUTHOR_ID);
            assertThat(author.nickname()).isEqualTo(null);
            assertThat(author.profileImageUrl()).isNull();
            assertThat(author.departmentName()).isNull();
            assertThat(author.representativeTrackName()).isNull();
            assertThat(author.isWithdrawn()).isTrue();
        }

        @Test
        @DisplayName("목록 조회: 탈퇴 작성자 게시글 조회 시 예외 없이 정상 응답한다")
        void getListDoesNotThrowForWithdrawnAuthor() {
            when(boardRepository.findByCodeAndActiveTrue("GENERAL"))
                    .thenReturn(Optional.of(Board.of("GENERAL", "자유/정보 게시판")));

            PostListQueryResult result = new PostListQueryResult(
                    1L, "title", "본문", null,
                    WITHDRAWN_AUTHOR_ID, null, null, null, null, null,
                    true,
                    0, 0, 0, 0, Instant.now()
            );
            when(postRepository.findPostList(eq("GENERAL"), any(Pageable.class), any(PostSearchCondition.class)))
                    .thenReturn(new PageImpl<>(List.of(result)));
            when(postTagQueryService.getTagNamesByPostIds(any())).thenReturn(Map.of());
            when(postViewerStateService.resolveForPosts(any(), any(), any())).thenReturn(Map.of());

            org.assertj.core.api.Assertions.assertThatCode(
                    () -> postQueryService.getList("GENERAL", PageRequest.of(0, 10), null, null, null, null)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("상세 조회: 탈퇴 작성자는 닉네임 등 null·isWithdrawn=true 가 매핑된다")
        void getDetailWithWithdrawnAuthorMapsAnonymizedFields() {
            PostDetailQueryResult result = new PostDetailQueryResult(
                    1L, "title", "content", null,
                    WITHDRAWN_AUTHOR_ID, null, null, null, null, null,
                    true,
                    0L, 5L, 2L, 3L,
                    Instant.now(), Instant.now(),
                    List.of(), List.of()
            );

            when(postRepository.findPostDetail(1L)).thenReturn(Optional.of(result));
            when(postViewerStateService.resolveForPosts(
                    isNull(), eq(List.of(1L)), eq(Map.of(1L, WITHDRAWN_AUTHOR_ID))))
                    .thenReturn(Map.of(1L, PostViewerResponse.guest()));

            PostDetailResponse response = postQueryService.getDetail(1L, null);

            assertThat(response.author().authorId()).isEqualTo(WITHDRAWN_AUTHOR_ID);
            assertThat(response.author().nickname()).isNull();
            assertThat(response.author().profileImageUrl()).isNull();
            assertThat(response.author().departmentName()).isNull();
            assertThat(response.author().representativeTrackName()).isNull();
            assertThat(response.author().isWithdrawn()).isTrue();
            // 게시글 카운트는 그대로 유지
            assertThat(response.likeCount()).isEqualTo(5L);
            assertThat(response.scrapCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("상세 조회: 탈퇴 작성자 게시글 조회 시 예외 없이 정상 응답한다")
        void getDetailDoesNotThrowForWithdrawnAuthor() {
            PostDetailQueryResult result = new PostDetailQueryResult(
                    1L, "title", "content", null,
                    WITHDRAWN_AUTHOR_ID, null, null, null, null, null,
                    true,
                    0L, 0L, 0L, 0L,
                    Instant.now(), Instant.now(),
                    List.of(), List.of()
            );

            when(postRepository.findPostDetail(1L)).thenReturn(Optional.of(result));
            when(postViewerStateService.resolveForPosts(
                    isNull(), eq(List.of(1L)), eq(Map.of(1L, WITHDRAWN_AUTHOR_ID))))
                    .thenReturn(Map.of(1L, PostViewerResponse.guest()));

            org.assertj.core.api.Assertions.assertThatCode(
                    () -> postQueryService.getDetail(1L, null)
            ).doesNotThrowAnyException();
        }
    }
}
