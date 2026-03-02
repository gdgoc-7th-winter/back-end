package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostCommentRequest;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostComment;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCommentCommandServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentRepository commentRepository;

    @InjectMocks
    private PostCommentCommandService postCommentCommandService;

    @Test
    @DisplayName("루트 댓글 생성 시 댓글 수가 증가한다")
    void createRootCommentIncrementsCount() {
        User user = buildUser(1L, "user");
        Post post = buildPost(1L, user);
        PostCommentRequest request = new PostCommentRequest("content", null);

        when(postRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(post));

        PostComment saved = PostComment.createRoot(post, user, "content");
        ReflectionTestUtils.setField(saved, "id", 10L);
        when(commentRepository.save(any(PostComment.class))).thenReturn(saved);

        Long result = postCommentCommandService.create(1L, request, user);

        assertThat(result).isEqualTo(10L);
        assertThat(post.getCommentCount()).isEqualTo(1);
        verify(commentRepository).save(any(PostComment.class));
    }

    @Test
    @DisplayName("부모 댓글이 1단계 이상이면 대댓글 생성이 실패한다")
    void createReplyFailsWhenParentDepthTooDeep() {
        User user = buildUser(1L, "user");
        Post post = buildPost(1L, user);
        PostCommentRequest request = new PostCommentRequest("reply", 20L);

        PostComment parent = PostComment.builder()
                .post(post)
                .user(user)
                .parentComment(null)
                .depth(1)
                .content("parent")
                .build();
        ReflectionTestUtils.setField(parent, "id", 20L);

        when(postRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findActiveById(20L)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> postCommentCommandService.create(1L, request, user))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);

        verify(commentRepository, never()).save(any(PostComment.class));
    }

    @Test
    @DisplayName("댓글 삭제는 작성자만 가능하다")
    void softDeleteChecksOwnership() {
        User author = buildUser(1L, "author");
        User other = buildUser(2L, "other");
        Post post = buildPost(1L, author);

        PostComment comment = PostComment.createRoot(post, author, "content");
        ReflectionTestUtils.setField(comment, "id", 100L);

        when(postRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findActiveById(100L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> postCommentCommandService.softDelete(1L, 100L, other))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("댓글 삭제 시 소프트 삭제 및 댓글 수 감소")
    void softDeleteUpdatesState() {
        User author = buildUser(1L, "author");
        Post post = buildPost(1L, author);
        ReflectionTestUtils.setField(post, "commentCount", 3);

        PostComment comment = PostComment.createRoot(post, author, "content");
        ReflectionTestUtils.setField(comment, "id", 100L);

        when(postRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findActiveById(100L)).thenReturn(Optional.of(comment));

        postCommentCommandService.softDelete(1L, 100L, author);

        assertThat(comment.isDeleted()).isTrue();
        assertThat(post.getCommentCount()).isEqualTo(2);
    }

    private static User buildUser(Long id, String nickname) {
        User user = new User("user@test.com", "pw");
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "nickname", nickname);
        return user;
    }

    private static Post buildPost(Long id, User author) {
        Board board = Board.of("general", "자유게시판");
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
