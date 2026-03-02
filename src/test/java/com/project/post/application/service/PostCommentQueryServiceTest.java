package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostCommentResponse;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostComment;
import com.project.post.application.service.impl.PostCommentQueryServiceImpl;
import com.project.post.domain.repository.PostCommentRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCommentQueryServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentRepository commentRepository;

    @InjectMocks
    private PostCommentQueryServiceImpl postCommentQueryService;

    @Test
    @DisplayName("게시글이 없으면 댓글 조회는 예외를 던진다")
    void getCommentsThrowsWhenPostMissing() {
        when(postRepository.existsActiveById(1L)).thenReturn(false);

        assertThatThrownBy(() -> postCommentQueryService.getComments(1L, PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 조회는 루트 댓글과 대댓글을 매핑한다")
    void getCommentsMapsReplies() {
        User user = buildUser(1L, "user");
        Post post = buildPost(1L, user);

        PostComment root = PostComment.createRoot(post, user, "root");
        ReflectionTestUtils.setField(root, "id", 10L);

        PostComment reply = PostComment.createReply(post, user, root, "reply");
        ReflectionTestUtils.setField(reply, "id", 11L);

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(commentRepository.findRootComments(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(root)));
        when(commentRepository.findRepliesByParentIds(any()))
                .thenReturn(List.of(reply));

        Page<PostCommentResponse> result = postCommentQueryService.getComments(1L, PageRequest.of(0, 10));

        PostCommentResponse rootResponse = result.getContent().get(0);
        assertThat(rootResponse.parentCommentId()).isNull();
        assertThat(rootResponse.replies()).hasSize(1);
        assertThat(rootResponse.replies().get(0).parentCommentId()).isEqualTo(10L);
        assertThat(rootResponse.replies().get(0).content()).isEqualTo("reply");
        assertThat(rootResponse.isDeleted()).isFalse();
        assertThat(rootResponse.hasMoreReplies()).isFalse();
    }

    @Test
    @DisplayName("삭제된 댓글은 isDeleted true, content·작성자 정보 null로 반환한다")
    void getCommentsReturnsDeletedCommentWithHiddenAuthor() {
        User user = buildUser(1L, "user");
        Post post = buildPost(1L, user);

        PostComment root = PostComment.createRoot(post, user, "deleted content");
        ReflectionTestUtils.setField(root, "id", 10L);
        root.softDelete();

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(commentRepository.findRootComments(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(root)));
        when(commentRepository.findRepliesByParentIds(any()))
                .thenReturn(List.of());

        Page<PostCommentResponse> result = postCommentQueryService.getComments(1L, PageRequest.of(0, 10));

        PostCommentResponse rootResponse = result.getContent().get(0);
        assertThat(rootResponse.isDeleted()).isTrue();
        assertThat(rootResponse.content()).isNull();
        assertThat(rootResponse.userId()).isNull();
        assertThat(rootResponse.userNickname()).isNull();
        assertThat(rootResponse.hasMoreReplies()).isFalse();
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
