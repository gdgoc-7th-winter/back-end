package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.CommentViewerResponse;
import com.project.post.application.dto.PostCommentResponse;
import com.project.post.application.dto.PostCommentRootListResponse;
import com.project.post.application.service.impl.PostCommentQueryServiceImpl;
import com.project.post.domain.constants.PostConstants;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.dto.ReplyPreviewRow;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCommentQueryServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentRepository commentRepository;

    @Mock
    private CommentViewerStateService commentViewerStateService;

    @InjectMocks
    private PostCommentQueryServiceImpl postCommentQueryService;

    @BeforeEach
    void stubCommentViewerGuest() {
        lenient().when(commentViewerStateService.resolveForComments(any(), any(), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Collection<Long> ids = (Collection<Long>) invocation.getArgument(1);
                    CommentViewerResponse guest = CommentViewerResponse.guest();
                    return ids.stream().collect(Collectors.toMap(id -> id, id -> guest));
                });
    }

    @Test
    @DisplayName("게시글이 없으면 댓글 조회는 예외를 던진다")
    void getCommentsThrowsWhenPostMissing() {
        when(postRepository.findActiveById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postCommentQueryService.getComments(1L, null, 10, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 조회는 최상위 댓글과 답글을 매핑하고 totalCommentCount를 포함한다")
    void getCommentsMapsReplies() {
        User user = buildUser(1L, "user");
        Post post = buildPost(1L, user);
        ReflectionTestUtils.setField(post, "commentCount", 12L);

        PostComment root = PostComment.createRoot(post, user, "root");
        ReflectionTestUtils.setField(root, "id", 10L);

        PostComment reply = PostComment.createReply(post, user, root, "reply");
        ReflectionTestUtils.setField(reply, "id", 11L);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findRootCommentsWithCursor(eq(1L), isNull(), isNull(), eq(11)))
                .thenReturn(List.of(root));
        when(commentRepository.findReplyPreviewRows(
                eq(1L), eq(List.of(10L)), eq(PostConstants.REPLY_PREVIEW_LIMIT + 1)))
                .thenReturn(List.of(new ReplyPreviewRow(11L, 10L)));
        when(commentRepository.findAllByIdInWithAssociations(eq(List.of(11L))))
                .thenReturn(List.of(reply));

        PostCommentRootListResponse result = postCommentQueryService.getComments(1L, null, 10, null);

        assertThat(result.totalCommentCount()).isEqualTo(12L);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        PostCommentResponse rootResponse = result.comments().get(0);
        assertThat(rootResponse.parentCommentId()).isNull();
        assertThat(rootResponse.replies()).hasSize(1);
        assertThat(rootResponse.replies().get(0).parentCommentId()).isEqualTo(10L);
        assertThat(rootResponse.replies().get(0).content()).isEqualTo("reply");
        assertThat(rootResponse.isDeleted()).isFalse();
        assertThat(rootResponse.isWithdrawn()).isFalse();
        assertThat(rootResponse.replies().get(0).isWithdrawn()).isFalse();
        assertThat(rootResponse.hasMoreReplies()).isFalse();
        assertThat(rootResponse.viewer()).isEqualTo(CommentViewerResponse.guest());
        assertThat(rootResponse.replies().get(0).viewer()).isEqualTo(CommentViewerResponse.guest());
    }

    @Test
    @DisplayName("삭제된 댓글은 목록에 포함되나 isDeleted true, content·작성자 정보 null로 마스킹 (원문 미노출)")
    void getCommentsReturnsDeletedCommentWithHiddenAuthor() {
        User user = buildUser(1L, "user");
        Post post = buildPost(1L, user);
        ReflectionTestUtils.setField(post, "commentCount", 1L);

        PostComment root = PostComment.createRoot(post, user, "deleted content");
        ReflectionTestUtils.setField(root, "id", 10L);
        root.softDelete();

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findRootCommentsWithCursor(eq(1L), isNull(), isNull(), eq(11)))
                .thenReturn(List.of(root));
        when(commentRepository.findReplyPreviewRows(
                eq(1L), eq(List.of(10L)), eq(PostConstants.REPLY_PREVIEW_LIMIT + 1)))
                .thenReturn(List.of());

        PostCommentRootListResponse result = postCommentQueryService.getComments(1L, null, 10, null);

        PostCommentResponse rootResponse = result.comments().get(0);
        assertThat(rootResponse.isDeleted()).isTrue();
        assertThat(rootResponse.content()).isNull();
        assertThat(rootResponse.userId()).isNull();
        assertThat(rootResponse.userNickname()).isNull();
        assertThat(rootResponse.isWithdrawn()).isFalse();
        assertThat(rootResponse.hasMoreReplies()).isFalse();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Long, Long>> authorMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(commentViewerStateService).resolveForComments(isNull(), any(), authorMapCaptor.capture());
        assertThat(authorMapCaptor.getValue()).doesNotContainKey(10L);
    }

    // ── 탈퇴 사용자가 댓글 작성자인 경우 ──────────────────────────────────────

    /**
     * 탈퇴 사용자가 작성자인 비삭제 댓글: 엔티티 연관 User 를 로드해 닉네임·{@link PostCommentResponse#isWithdrawn()} 을 채운다.
     * {@code User.withdraw()} 는 닉네임을 비우고 soft-delete 하므로 닉네임은 null이 될 수 있다.
     * 삭제된 댓글({@code isDeleted=true})은 작성자 마스킹·isWithdrawn=false 규칙이 별도다.
     */
    @Nested
    @DisplayName("탈퇴 사용자가 댓글 작성자인 경우")
    class WithdrawnCommentAuthorTests {

        private User withdrawnUser;
        private Post post;

        @BeforeEach
        void setUpWithdrawn() {
            withdrawnUser = buildUser(99L, "original");
            withdrawnUser.withdraw(); // 닉네임 null, deletedAt 설정(탈퇴)
            post = buildPost(1L, withdrawnUser);
            ReflectionTestUtils.setField(post, "commentCount", 1L);
        }

        @Test
        @DisplayName("비삭제 댓글의 탈퇴 작성자: 닉네임 null·isWithdrawn true, 본문은 유지")
        void getCommentsShowsWithdrawnAuthorNicknameOnActiveComment() {
            PostComment root = PostComment.createRoot(post, withdrawnUser, "댓글 내용");
            ReflectionTestUtils.setField(root, "id", 10L);
            // 댓글 자체는 soft-delete 없음 → isDeleted = false

            when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
            when(commentRepository.findRootCommentsWithCursor(eq(1L), isNull(), isNull(), eq(11)))
                    .thenReturn(List.of(root));
            when(commentRepository.findReplyPreviewRows(
                    eq(1L), eq(List.of(10L)), eq(PostConstants.REPLY_PREVIEW_LIMIT + 1)))
                    .thenReturn(List.of());

            PostCommentRootListResponse result = postCommentQueryService.getComments(1L, null, 10, null);

            PostCommentResponse response = result.comments().get(0);
            assertThat(response.isDeleted()).isFalse();        // 댓글 자체는 삭제되지 않음
            assertThat(response.userId()).isEqualTo(99L);      // FK는 여전히 유효
            assertThat(response.userNickname()).isEqualTo(null);
            assertThat(response.isWithdrawn()).isTrue();
            assertThat(response.content()).isEqualTo("댓글 내용"); // 내용은 그대로 표시
        }

        @Test
        @DisplayName("비삭제 댓글의 탈퇴 작성자 조회 시 예외 없이 정상 응답한다")
        void getCommentsDoesNotThrowForWithdrawnCommentAuthor() {
            PostComment root = PostComment.createRoot(post, withdrawnUser, "댓글 내용");
            ReflectionTestUtils.setField(root, "id", 10L);

            when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
            when(commentRepository.findRootCommentsWithCursor(eq(1L), isNull(), isNull(), eq(11)))
                    .thenReturn(List.of(root));
            when(commentRepository.findReplyPreviewRows(
                    eq(1L), eq(List.of(10L)), eq(PostConstants.REPLY_PREVIEW_LIMIT + 1)))
                    .thenReturn(List.of());

            assertThatCode(() -> postCommentQueryService.getComments(1L, null, 10, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("탈퇴 사용자의 답글: 닉네임 null·isWithdrawn true")
        void getCommentsShowsWithdrawnAuthorInReply() {
            User activeRootAuthor = buildUser(1L, "activeUser");
            Post postByActive = buildPost(1L, activeRootAuthor);
            ReflectionTestUtils.setField(postByActive, "commentCount", 2L);

            PostComment root = PostComment.createRoot(postByActive, activeRootAuthor, "부모 댓글");
            ReflectionTestUtils.setField(root, "id", 10L);

            PostComment withdrawnReply = PostComment.createReply(postByActive, withdrawnUser, root, "탈퇴자의 답글");
            ReflectionTestUtils.setField(withdrawnReply, "id", 11L);

            when(postRepository.findActiveById(1L)).thenReturn(Optional.of(postByActive));
            when(commentRepository.findRootCommentsWithCursor(eq(1L), isNull(), isNull(), eq(11)))
                    .thenReturn(List.of(root));
            when(commentRepository.findReplyPreviewRows(
                    eq(1L), eq(List.of(10L)), eq(PostConstants.REPLY_PREVIEW_LIMIT + 1)))
                    .thenReturn(List.of(new ReplyPreviewRow(11L, 10L)));
            when(commentRepository.findAllByIdInWithAssociations(eq(List.of(11L))))
                    .thenReturn(List.of(withdrawnReply));

            PostCommentRootListResponse result = postCommentQueryService.getComments(1L, null, 10, null);

            PostCommentResponse rootResponse = result.comments().get(0);
            assertThat(rootResponse.userNickname()).isEqualTo("activeUser"); // 루트 작성자는 정상
            assertThat(rootResponse.isWithdrawn()).isFalse();
            assertThat(rootResponse.replies()).hasSize(1);

            PostCommentResponse replyResponse = rootResponse.replies().get(0);
            assertThat(replyResponse.isDeleted()).isFalse();
            assertThat(replyResponse.userId()).isEqualTo(99L);
            assertThat(replyResponse.userNickname()).isEqualTo(null);
            assertThat(replyResponse.isWithdrawn()).isTrue();
            assertThat(replyResponse.content()).isEqualTo("탈퇴자의 답글");
            assertThat(replyResponse.parentCommentId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("탈퇴/활성 작성자 댓글 혼재 시 각각 올바른 닉네임을 반환한다")
        void getCommentsMixedWithdrawnAndActiveAuthors() {
            User activeUser = buildUser(1L, "activeUser");
            Post mixedPost = buildPost(1L, activeUser);
            ReflectionTestUtils.setField(mixedPost, "commentCount", 2L);

            PostComment withdrawnRoot = PostComment.createRoot(mixedPost, withdrawnUser, "탈퇴자 댓글");
            ReflectionTestUtils.setField(withdrawnRoot, "id", 10L);

            PostComment activeRoot = PostComment.createRoot(mixedPost, activeUser, "활성 댓글");
            ReflectionTestUtils.setField(activeRoot, "id", 11L);

            when(postRepository.findActiveById(1L)).thenReturn(Optional.of(mixedPost));
            when(commentRepository.findRootCommentsWithCursor(eq(1L), isNull(), isNull(), eq(11)))
                    .thenReturn(List.of(withdrawnRoot, activeRoot));
            when(commentRepository.findReplyPreviewRows(
                    eq(1L), eq(List.of(10L, 11L)), eq(PostConstants.REPLY_PREVIEW_LIMIT + 1)))
                    .thenReturn(List.of());

            PostCommentRootListResponse result = postCommentQueryService.getComments(1L, null, 10, null);

            assertThat(result.comments()).hasSize(2);
            PostCommentResponse withdrawnResponse = result.comments().get(0);
            assertThat(withdrawnResponse.userNickname()).isEqualTo(null);
            assertThat(withdrawnResponse.userId()).isEqualTo(99L);
            assertThat(withdrawnResponse.isDeleted()).isFalse();
            assertThat(withdrawnResponse.isWithdrawn()).isTrue();

            PostCommentResponse activeResponse = result.comments().get(1);
            assertThat(activeResponse.userNickname()).isEqualTo("activeUser");
            assertThat(activeResponse.isDeleted()).isFalse();
            assertThat(activeResponse.isWithdrawn()).isFalse();
        }

        @Test
        @DisplayName("탈퇴한 댓글 작성자 정보가 viewer 상태 해결에 사용되는 authorMap에 포함된다")
        void withdrawnAuthorIdIsIncludedInAuthorMapForViewerResolution() {
            PostComment root = PostComment.createRoot(post, withdrawnUser, "댓글 내용");
            ReflectionTestUtils.setField(root, "id", 10L);

            when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
            when(commentRepository.findRootCommentsWithCursor(eq(1L), isNull(), isNull(), eq(11)))
                    .thenReturn(List.of(root));
            when(commentRepository.findReplyPreviewRows(
                    eq(1L), eq(List.of(10L)), eq(PostConstants.REPLY_PREVIEW_LIMIT + 1)))
                    .thenReturn(List.of());

            postCommentQueryService.getComments(1L, null, 10, null);

            // 비삭제 댓글이므로 탈퇴 작성자 ID도 authorMap에 포함되어 viewer 해결에 쓰임
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<Long, Long>> authorMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(commentViewerStateService).resolveForComments(isNull(), any(), authorMapCaptor.capture());
            assertThat(authorMapCaptor.getValue()).containsEntry(10L, 99L);
        }
    }

    private static User buildUser(Long id, String nickname) {
        User user = User.builder().email("user@test.com").password("pw").nickname("testuser1").build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "nickname", nickname);
        return user;
    }

    private static Post buildPost(Long id, User author) {
        Board board = Board.of("GENERAL", "자유/정보 게시판");
        ReflectionTestUtils.setField(board, "id", 10L);
        return Post.builder()
                .id(id)
                .board(board)
                .author(author)
                .title("title")
                .content("content")
                .build();
    }
}
