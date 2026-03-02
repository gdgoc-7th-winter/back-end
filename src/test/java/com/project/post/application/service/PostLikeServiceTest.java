package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostLike;
import com.project.post.domain.repository.PostLikeRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PostLikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostLikeService postLikeService;

    @Test
    @DisplayName("게시글이 없으면 좋아요 토글 시 예외를 던진다")
    void toggleThrowsWhenPostMissing() {
        when(postRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.toggle(1L, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verifyNoInteractions(postLikeRepository);
    }

    @Test
    @DisplayName("좋아요가 없으면 추가하고 true를 반환한다")
    void toggleAddsLikeWhenNotExists() {
        User user = buildUser(1L);
        Post post = buildPost(1L, user);
        ReflectionTestUtils.setField(post, "likeCount", 0);

        when(postRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        LikeScrapToggleResponse result = postLikeService.toggle(1L, user);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        assertThat(post.getLikeCount()).isEqualTo(1);
        verify(postLikeRepository).save(any(PostLike.class));
        verify(postLikeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("좋아요가 있으면 제거하고 false를 반환한다")
    void toggleRemovesLikeWhenExists() {
        User user = buildUser(1L);
        Post post = buildPost(1L, user);
        ReflectionTestUtils.setField(post, "likeCount", 1);
        PostLike like = PostLike.of(post, user);
        ReflectionTestUtils.setField(like, "id", 10L);

        when(postRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));

        LikeScrapToggleResponse result = postLikeService.toggle(1L, user);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        assertThat(post.getLikeCount()).isEqualTo(0);
        verify(postLikeRepository).delete(like);
        verify(postLikeRepository, never()).save(any());
    }

    private static User buildUser(Long id) {
        User user = new User("user@test.com", "pw");
        ReflectionTestUtils.setField(user, "id", id);
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
