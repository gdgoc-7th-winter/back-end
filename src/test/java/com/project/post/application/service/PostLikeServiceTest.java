package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.service.ContributionFacade;
import com.project.global.event.ActivityType;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.service.impl.PostLikeServiceImpl;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private ContributionFacade contributionFacade;

    @InjectMocks
    private PostLikeServiceImpl postLikeService;

    @Test
    @DisplayName("게시글이 없으면 좋아요 추가 시 예외를 던진다")
    void likeThrowsWhenPostMissing() {
        when(postRepository.findActiveById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.like(1L, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verifyNoInteractions(postLikeRepository);
    }

    @Test
    @DisplayName("좋아요가 없으면 insert 후 카운트를 증가시킨다")
    void likeInsertsAndIncrementsCount() {
        User liker = buildUser(1L);
        User author = buildUser(2L);
        Post post = buildPost(1L, author);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.insertIfAbsent(1L, 1L)).thenReturn(1);
        when(postRepository.incrementLikeCount(1L)).thenReturn(1);
        PostLike like = PostLike.of(post, liker);
        ReflectionTestUtils.setField(like, "id", 99L);
        when(postLikeRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));
        when(postRepository.findLikeCountById(1L)).thenReturn(Optional.of(1L));

        LikeScrapToggleResponse result = postLikeService.like(1L, liker);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        verify(postRepository).incrementLikeCount(1L);
        verify(contributionFacade)
                .applyActivity(
                        argThat(
                                (ActivityContext c) ->
                                        c.activityType() == ActivityType.LIKE_PRESSED
                                                && c.subjectUserId() == 2L
                                                && c.referenceId() == 99L));
    }

    @Test
    @DisplayName("이미 좋아요가 있으면 insert 무시(멱등) + 카운트 미증가")
    void likeIsIdempotentWhenAlreadyLiked() {
        User user = buildUser(1L);
        Post post = buildPost(1L, buildUser(2L));

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.insertIfAbsent(1L, 1L)).thenReturn(0);
        when(postRepository.findLikeCountById(1L)).thenReturn(Optional.of(1L));

        LikeScrapToggleResponse result = postLikeService.like(1L, user);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        verify(postRepository, never()).incrementLikeCount(1L);
        verify(contributionFacade, never()).applyActivity(any());
    }

    @Test
    @DisplayName("좋아요가 있으면 delete 후 카운트를 감소시킨다")
    void unlikeDeletesAndDecrementsCount() {
        User liker = buildUser(1L);
        Post post = buildPost(1L, buildUser(2L));
        PostLike like = PostLike.of(post, liker);
        ReflectionTestUtils.setField(like, "id", 99L);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));
        when(postLikeRepository.deleteByPostIdAndUserId(1L, 1L)).thenReturn(1);
        when(postRepository.decrementLikeCount(1L)).thenReturn(1);
        when(postRepository.findLikeCountById(1L)).thenReturn(Optional.of(0L));

        LikeScrapToggleResponse result = postLikeService.unlike(1L, liker);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        verify(postRepository).decrementLikeCount(1L);
        verify(contributionFacade)
                .applyActivity(
                        argThat(
                                (ActivityContext c) ->
                                        c.activityType() == ActivityType.LIKE_CANCELLED
                                                && c.subjectUserId() == 2L
                                                && c.referenceId() == 99L));
    }

    @Test
    @DisplayName("좋아요가 없으면 delete 무시(멱등) + 카운트 미감소")
    void unlikeIsIdempotentWhenNotLiked() {
        User user = buildUser(1L);
        Post post = buildPost(1L, buildUser(2L));

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(postRepository.findLikeCountById(1L)).thenReturn(Optional.of(0L));

        LikeScrapToggleResponse result = postLikeService.unlike(1L, user);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        verify(postRepository, never()).decrementLikeCount(1L);
        verify(contributionFacade, never()).applyActivity(any());
    }

    private static User buildUser(Long id) {
        User user = User.builder().email("user@test.com").password("pw").nickname("testuser").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Post buildPost(Long id, User author) {
        Board board = Board.of("GENERAL", "자유");
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
