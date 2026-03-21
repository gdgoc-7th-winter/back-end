package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.service.impl.PostScrapServiceImpl;
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
    private PostScrapServiceImpl postScrapService;

    @Test
    @DisplayName("게시글이 없으면 스크랩 추가 시 예외를 던진다")
    void scrapThrowsWhenPostMissing() {
        when(postRepository.existsActiveById(1L)).thenReturn(false);

        assertThatThrownBy(() -> postScrapService.scrap(1L, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verifyNoInteractions(postScrapRepository);
    }

    @Test
    @DisplayName("스크랩이 없으면 insert 후 카운트를 증가시킨다")
    void scrapInsertsAndIncrementsCount() {
        User user = buildUser(1L);

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(postScrapRepository.insertIfAbsent(1L, 1L)).thenReturn(1);
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(1L));

        LikeScrapToggleResponse result = postScrapService.scrap(1L, user);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        verify(postRepository).incrementScrapCount(1L);
    }

    @Test
    @DisplayName("이미 스크랩이 있으면 insert 무시(멱등) + 카운트 미증가")
    void scrapIsIdempotentWhenAlreadyScrapped() {
        User user = buildUser(1L);

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(postScrapRepository.insertIfAbsent(1L, 1L)).thenReturn(0);
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(1L));

        LikeScrapToggleResponse result = postScrapService.scrap(1L, user);

        assertThat(result.liked()).isTrue();
        assertThat(result.count()).isEqualTo(1);
        verify(postRepository, never()).incrementScrapCount(1L);
    }

    @Test
    @DisplayName("스크랩이 있으면 delete 후 카운트를 감소시킨다")
    void unscrapDeletesAndDecrementsCount() {
        User user = buildUser(1L);

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(postScrapRepository.deleteByPostIdAndUserId(1L, 1L)).thenReturn(1);
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(0L));

        LikeScrapToggleResponse result = postScrapService.unscrap(1L, user);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        verify(postRepository).decrementScrapCount(1L);
    }

    @Test
    @DisplayName("스크랩이 없으면 delete 무시(멱등) + 카운트 미감소")
    void unscrapIsIdempotentWhenNotScrapped() {
        User user = buildUser(1L);

        when(postRepository.existsActiveById(1L)).thenReturn(true);
        when(postScrapRepository.deleteByPostIdAndUserId(1L, 1L)).thenReturn(0);
        when(postRepository.findScrapCountById(1L)).thenReturn(Optional.of(0L));

        LikeScrapToggleResponse result = postScrapService.unscrap(1L, user);

        assertThat(result.liked()).isFalse();
        assertThat(result.count()).isEqualTo(0);
        verify(postRepository, never()).decrementScrapCount(1L);
    }

    private static User buildUser(Long id) {
        User user = new User("user@test.com", "pw", "testuser");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
