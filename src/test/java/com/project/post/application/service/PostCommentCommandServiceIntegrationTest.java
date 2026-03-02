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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        PostCommentCommandService.class,
        PostCommentCommandServiceIntegrationTest.TestConfig.class
})
class PostCommentCommandServiceIntegrationTest {

    @Autowired
    private PostCommentCommandService postCommentCommandService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Test
    @DisplayName("부모 댓글이 다른 게시글에 속하면 댓글 생성이 실패한다")
    void createReplyFailsWhenParentBelongsToDifferentPost() {
        User user = new User("user@test.com", "pw");
        Board board = Board.of("general-it", "자유게시판");
        Post post = buildPost(1L, board, user, "title-1");
        Post otherPost = buildPost(2L, board, user, "title-2");
        PostComment parent = PostComment.builder()
                .id(20L)
                .post(otherPost)
                .user(user)
                .parentComment(null)
                .depth(0)
                .content("parent")
                .build();

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postCommentRepository.findActiveById(20L)).thenReturn(Optional.of(parent));

        PostCommentRequest request = new PostCommentRequest("reply", 20L);

        assertThatThrownBy(() -> postCommentCommandService.create(1L, request, user))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    private static Post buildPost(Long id, Board board, User author, String title) {
        return Post.builder()
                .id(id)
                .board(board)
                .author(author)
                .title(title)
                .content("content")
                .build();
    }

    @Configuration
    static class TestConfig {

        @Bean
        PostRepository postRepository() {
            return Mockito.mock(PostRepository.class);
        }

        @Bean
        PostCommentRepository postCommentRepository() {
            return Mockito.mock(PostCommentRepository.class);
        }
    }
}
