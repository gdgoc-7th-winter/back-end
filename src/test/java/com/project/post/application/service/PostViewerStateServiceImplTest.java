package com.project.post.application.service;

import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.service.impl.PostViewerStateServiceImpl;
import com.project.post.domain.repository.PostLikeRepository;
import com.project.post.domain.repository.PostScrapRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostViewerStateServiceImplTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostScrapRepository postScrapRepository;

    @InjectMocks
    private PostViewerStateServiceImpl postViewerStateService;

    @Test
    @DisplayName("비로그인이면 DB 조회 없이 모두 guest 뷰어 상태")
    void guestWithoutDb() {
        Map<Long, PostViewerResponse> map = postViewerStateService.resolveForPosts(
                null, List.of(1L, 2L), Map.of(1L, 99L, 2L, 10L));

        assertThat(map.get(1L)).isEqualTo(PostViewerResponse.guest());
        assertThat(map.get(2L)).isEqualTo(PostViewerResponse.guest());
        verifyNoInteractions(postLikeRepository, postScrapRepository);
    }

    @Test
    @DisplayName("로그인 시 좋아요·스크랩 ID를 배치 조회해 매핑한다 (Set 인자는 내용만 검증)")
    void mapsLikedAndScrapped() {
        when(postLikeRepository.findPostIdsLikedByUserAndPostIdIn(
                argThat(s -> s != null && s.size() == 2 && s.contains(1L) && s.contains(2L)),
                eq(99L)))
                .thenReturn(List.of(1L));
        when(postScrapRepository.findPostIdsScrappedByUserAndPostIdIn(
                argThat(s -> s != null && s.size() == 2 && s.contains(1L) && s.contains(2L)),
                eq(99L)))
                .thenReturn(List.of(2L));

        Map<Long, PostViewerResponse> map = postViewerStateService.resolveForPosts(
                99L, List.of(1L, 2L), Map.of(1L, 10L, 2L, 20L));

        assertThat(map.get(1L)).isEqualTo(new PostViewerResponse(true, false, false));
        assertThat(map.get(2L)).isEqualTo(new PostViewerResponse(false, true, false));
    }

    @Test
    @DisplayName("viewerUserId가 작성자 ID와 같으면 isAuthor(author)=true")
    void mapsAuthor() {
        when(postLikeRepository.findPostIdsLikedByUserAndPostIdIn(
                argThat((Set<Long> s) -> s != null && s.size() == 2 && s.contains(1L) && s.contains(2L)),
                eq(99L)))
                .thenReturn(List.of());
        when(postScrapRepository.findPostIdsScrappedByUserAndPostIdIn(
                argThat((Set<Long> s) -> s != null && s.size() == 2 && s.contains(1L) && s.contains(2L)),
                eq(99L)))
                .thenReturn(List.of());

        Map<Long, PostViewerResponse> map = postViewerStateService.resolveForPosts(
                99L, List.of(1L, 2L), Map.of(1L, 99L, 2L, 10L));

        assertThat(map.get(1L).author()).isTrue();
        assertThat(map.get(2L).author()).isFalse();
    }
}
