package com.project.post.application.service;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.port.ContributionOutboxPort;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.event.ActivityType;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.service.impl.PostScrapServiceImpl;
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
import static org.mockito.ArgumentMatchers.argThat;
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

    @Mock
    private ContributionOutboxPort contributionOutboxPort;

    @InjectMocks
    private PostScrapServiceImpl postScrapService;

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
    @DisplayName("스크랩이 없으면 insert 후 카운트를 증가시킨다")
    void scrapInsertsAndIncrementsCount() {
        User scraper = buildUser(1L);
        User author = buildUser(2L);
        Post post = buildPost(1L, author);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.insertIfAbsent(1L, 1L)).thenReturn(1);
        when(postRepository.incrementScrapCount(1L)).thenReturn(1);
        PostScrap scrap = PostScrap.of(post, scraper);
        ReflectionTestUtils.setField(scrap, "id", 99L);
        when(postScrapRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.of(scrap));
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(1L));

        LikeScrapToggleResponse result = postScrapService.scrap(1L, scraper);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        verify(postRepository).incrementScrapCount(1L);
        verify(contributionOutboxPort)
                .append(
                        argThat(
                                (ActivityContext c) ->
                                        c.activityType() == ActivityType.SCRAP_PRESSED
                                                && c.subjectUserId() == 2L
                                                && c.referenceId() == 99L));
    }

    @Test
    @DisplayName("이미 스크랩이 있으면 insert 무시(멱등) + 카운트 미증가")
    void scrapIsIdempotentWhenAlreadyScrapped() {
        User user = buildUser(1L);
        Post post = buildPost(1L, buildUser(2L));

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.insertIfAbsent(1L, 1L)).thenReturn(0);
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(1L));

        LikeScrapToggleResponse result = postScrapService.scrap(1L, user);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        verify(postRepository, never()).incrementScrapCount(1L);
        verify(contributionOutboxPort, never()).append(any());
    }

    @Test
    @DisplayName("스크랩이 있으면 delete 후 카운트를 감소시킨다")
    void unscrapDeletesAndDecrementsCount() {
        User scraper = buildUser(1L);
        Post post = buildPost(1L, buildUser(2L));
        PostScrap scrap = PostScrap.of(post, scraper);
        ReflectionTestUtils.setField(scrap, "id", 99L);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.of(scrap));
        when(postScrapRepository.deleteByPostIdAndUserId(1L, 1L)).thenReturn(1);
        when(postRepository.decrementScrapCount(1L)).thenReturn(1);
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(0L));

        LikeScrapToggleResponse result = postScrapService.unscrap(1L, scraper);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        verify(postRepository).decrementScrapCount(1L);
        verify(contributionOutboxPort, never()).append(any());
    }

    @Test
    @DisplayName("스크랩이 없으면 delete 무시(멱등) + 카운트 미감소")
    void unscrapIsIdempotentWhenNotScrapped() {
        User user = buildUser(1L);
        Post post = buildPost(1L, buildUser(2L));

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(0L));

        LikeScrapToggleResponse result = postScrapService.unscrap(1L, user);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        verify(postRepository, never()).decrementScrapCount(1L);
        verify(contributionOutboxPort, never()).append(any());
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
