package com.project.post.domain.entity;

import com.project.post.domain.exception.PostDomainException;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PostCommentTest {

    @Test
    @DisplayName("부모 댓글이 다른 게시글에 속하면 예외가 발생한다")
    void createReplyThrowsWhenParentBelongsToDifferentPost() {
        User user = User.builder().email("user@test.com").password("pw").nickname("testuser").build();
        Board board = Board.of("general", "자유게시판");
        Post post = buildPost(1L, board, user, "title-1");
        Post otherPost = buildPost(2L, board, user, "title-2");

        PostComment parent = PostComment.builder()
                .post(otherPost)
                .user(user)
                .parentComment(null)
                .depth(0)
                .content("parent")
                .build();

        assertThatThrownBy(() -> PostComment.createReply(post, user, parent, "reply"))
                .isInstanceOf(PostDomainException.class)
                .hasMessage("부모 댓글이 해당 게시글에 속하지 않습니다.");
    }

    @Test
    @DisplayName("부모 댓글 depth가 1 이상이면 예외가 발생한다")
    void createReplyThrowsWhenParentDepthTooDeep() {
        User user = User.builder().email("user@test.com").password("pw").nickname("testuser").build();
        Board board = Board.of("general", "자유게시판");
        Post post = buildPost(1L, board, user, "title");

        PostComment parent = PostComment.builder()
                .post(post)
                .user(user)
                .parentComment(null)
                .depth(1)
                .content("parent")
                .build();

        assertThatThrownBy(() -> PostComment.createReply(post, user, parent, "reply"))
                .isInstanceOf(PostDomainException.class)
                .hasMessage("답글은 최상위 댓글에만 달 수 있습니다.");
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
}
