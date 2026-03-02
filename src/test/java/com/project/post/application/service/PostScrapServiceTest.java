package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostScrap;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.PostScrapRepository;
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
class PostScrapServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostScrapRepository postScrapRepository;

    @InjectMocks
    private PostScrapService postScrapService;

    @Test
    @DisplayName("게시글이 없으면 스크랩 추가 시 예외를 던진다")
    void scrapThrowsWhenPostMissing() {
        when(postRepository.findActiveById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postScrapService.scrap(1L, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verifyNoInteractions(postScrapRepository);
    }

    @Test
    @DisplayName("스크랩이 없으면 추가하고 true를 반환한다")
    void scrapAddsScrapWhenNotExists() {
        User user = buildUser(1L);
        Post post = buildPost(1L, user);
        ReflectionTestUtils.setField(post, "scrapCount", 0);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(1));

        LikeScrapToggleResponse result = postScrapService.scrap(1L, user);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        verify(postScrapRepository).save(any(PostScrap.class));
        verify(postRepository).incrementScrapCount(1L);
        verify(postScrapRepository, never()).delete(any());
    }

    @Test
    @DisplayName("스크랩이 있으면 추가 요청 시 상태를 유지한다")
    void scrapKeepsStateWhenExists() {
        User user = buildUser(1L);
        Post post = buildPost(1L, user);
        ReflectionTestUtils.setField(post, "scrapCount", 1);
        PostScrap scrap = PostScrap.of(post, user);
        ReflectionTestUtils.setField(scrap, "id", 10L);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.of(scrap));
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(1));

        LikeScrapToggleResponse result = postScrapService.scrap(1L, user);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        verify(postScrapRepository, never()).delete(any());
        verify(postScrapRepository, never()).save(any());
    }

    @Test
    @DisplayName("스크랩이 있으면 취소하고 false를 반환한다")
    void unscrapRemovesScrapWhenExists() {
        User user = buildUser(1L);
        Post post = buildPost(1L, user);
        ReflectionTestUtils.setField(post, "scrapCount", 1);
        PostScrap scrap = PostScrap.of(post, user);
        ReflectionTestUtils.setField(scrap, "id", 10L);

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(postScrapRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.of(scrap));
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(0));

        LikeScrapToggleResponse result = postScrapService.unscrap(1L, user);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        verify(postScrapRepository).delete(scrap);
        verify(postRepository).decrementScrapCount(1L);
        verify(postScrapRepository, never()).save(any());
    }

    @Test
    @DisplayName("스크랩이 없으면 취소 요청 시 상태를 유지한다")
    void unscrapKeepsStateWhenNotExists() {
        User user = buildUser(1L);
        Post post = buildPost(1L, user);
        ReflectionTestUtils.setField(post, "scrapCount", 0);

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(postScrapRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(0));

        LikeScrapToggleResponse result = postScrapService.unscrap(1L, user);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        verify(postScrapRepository, never()).delete(any());
        verify(postScrapRepository, never()).save(any());
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
