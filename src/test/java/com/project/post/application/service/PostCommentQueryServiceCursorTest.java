package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.CommentViewerResponse;
import com.project.post.application.dto.PostCommentChildListResponse;
import com.project.post.application.service.impl.PostCommentQueryServiceImpl;
import com.project.post.application.support.PostCommentCursorCodec;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCommentQueryServiceCursorTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentRepository commentRepository;

    @Mock
    private CommentViewerStateService commentViewerStateService;

    @InjectMocks
    private PostCommentQueryServiceImpl postCommentQueryService;

    @BeforeEach
    void stubViewer() {
        lenient().when(commentViewerStateService.resolveForComments(any(), any(), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Collection<Long> ids = (Collection<Long>) invocation.getArgument(1);
                    CommentViewerResponse guest = CommentViewerResponse.guest();
                    return ids.stream().collect(Collectors.toMap(id -> id, id -> guest));
                });
    }

    @Test
    @DisplayName("부모가 최상위가 아니면 답글 목록 조회는 INVALID_INPUT")
    void rejectsNonRoot() {
        User u = buildUser(1L);
        Post post = buildPost(1L, u);
        PostComment root = PostComment.createRoot(post, u, "r");
        ReflectionTestUtils.setField(root, "id", 10L);
        PostComment reply = PostComment.createReply(post, u, root, "x");
        ReflectionTestUtils.setField(reply, "id", 11L);

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(commentRepository.findById(11L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> postCommentQueryService.getChildComments(1L, 11L, null, 10, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("첫 페이지: size개만 반환하고 hasNext면 nextCursor 존재")
    void firstPageHasNext() {
        User u = buildUser(1L);
        Post post = buildPost(1L, u);
        PostComment root = PostComment.createRoot(post, u, "r");
        ReflectionTestUtils.setField(root, "id", 10L);

        PostComment a = PostComment.createReply(post, u, root, "a");
        ReflectionTestUtils.setField(a, "id", 101L);
        ReflectionTestUtils.setField(a, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        PostComment b = PostComment.createReply(post, u, root, "b");
        ReflectionTestUtils.setField(b, "id", 102L);
        ReflectionTestUtils.setField(b, "createdAt", Instant.parse("2026-01-01T00:00:01Z"));
        PostComment c = PostComment.createReply(post, u, root, "c");
        ReflectionTestUtils.setField(c, "id", 103L);
        ReflectionTestUtils.setField(c, "createdAt", Instant.parse("2026-01-01T00:00:02Z"));

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(root));
        when(commentRepository.findCommentsByParentWithCursor(eq(1L), eq(10L), eq(null), eq(null), eq(3)))
                .thenReturn(List.of(a, b, c));

        PostCommentChildListResponse page =
                postCommentQueryService.getChildComments(1L, 10L, null, 2, null);

        assertThat(page.comments()).hasSize(2);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isNotNull();
        PostCommentCursorCodec.Cursor dec = PostCommentCursorCodec.decode(page.nextCursor());
        assertThat(dec.id()).isEqualTo(102L);
    }

    @Test
    @DisplayName("마지막 페이지: hasNext false, nextCursor null")
    void lastPage() {
        User u = buildUser(1L);
        Post post = buildPost(1L, u);
        PostComment root = PostComment.createRoot(post, u, "r");
        ReflectionTestUtils.setField(root, "id", 10L);
        PostComment a = PostComment.createReply(post, u, root, "a");
        ReflectionTestUtils.setField(a, "id", 101L);
        ReflectionTestUtils.setField(a, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(root));
        when(commentRepository.findCommentsByParentWithCursor(eq(1L), eq(10L), eq(null), eq(null), eq(3)))
                .thenReturn(List.of(a));

        PostCommentChildListResponse page =
                postCommentQueryService.getChildComments(1L, 10L, null, 2, null);

        assertThat(page.comments()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    private static User buildUser(Long id) {
        User user = new User("u@test.com", "pw", "u");
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "nickname", "n");
        return user;
    }

    private static Post buildPost(Long id, User author) {
        Board board = Board.of("G", "b");
        ReflectionTestUtils.setField(board, "id", 1L);
        return Post.builder()
                .id(id)
                .board(board)
                .author(author)
                .title("t")
                .content("c")
                .build();
    }
}
