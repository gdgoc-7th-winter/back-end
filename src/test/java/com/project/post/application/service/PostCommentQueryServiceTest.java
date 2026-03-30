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
        assertThat(rootResponse.hasMoreReplies()).isFalse();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Long, Long>> authorMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(commentViewerStateService).resolveForComments(isNull(), any(), authorMapCaptor.capture());
        assertThat(authorMapCaptor.getValue()).doesNotContainKey(10L);
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
