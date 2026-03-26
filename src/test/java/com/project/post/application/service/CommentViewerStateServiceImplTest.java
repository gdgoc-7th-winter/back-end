package com.project.post.application.service;

import com.project.post.application.dto.CommentViewerResponse;
import com.project.post.application.service.impl.CommentViewerStateServiceImpl;
import com.project.post.domain.repository.PostCommentLikeRepository;
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
class CommentViewerStateServiceImplTest {

    @Mock
    private PostCommentLikeRepository postCommentLikeRepository;

    @InjectMocks
    private CommentViewerStateServiceImpl commentViewerStateService;

    @Test
    @DisplayName("비로그인이면 DB 조회 없이 모두 guest 뷰어 상태")
    void guestWithoutDb() {
        Map<Long, CommentViewerResponse> map = commentViewerStateService.resolveForComments(
                null, List.of(1L, 2L), Map.of(1L, 99L, 2L, 10L));

        assertThat(map.get(1L)).isEqualTo(CommentViewerResponse.guest());
        assertThat(map.get(2L)).isEqualTo(CommentViewerResponse.guest());
        verifyNoInteractions(postCommentLikeRepository);
    }

    @Test
    @DisplayName("로그인 시 좋아요 ID를 배치 조회해 매핑한다")
    void mapsLiked() {
        when(postCommentLikeRepository.findCommentIdsLikedByUserAndCommentIdIn(
                argThat(s -> s != null && s.size() == 2 && s.contains(1L) && s.contains(2L)),
                eq(99L)))
                .thenReturn(List.of(1L));

        Map<Long, CommentViewerResponse> map = commentViewerStateService.resolveForComments(
                99L, List.of(1L, 2L), Map.of(1L, 10L, 2L, 20L));

        assertThat(map.get(1L)).isEqualTo(new CommentViewerResponse(true, false));
        assertThat(map.get(2L)).isEqualTo(new CommentViewerResponse(false, false));
    }

    @Test
    @DisplayName("viewerUserId가 작성자 ID와 같으면 isAuthor(author)=true")
    void mapsAuthor() {
        when(postCommentLikeRepository.findCommentIdsLikedByUserAndCommentIdIn(
                argThat((Set<Long> s) -> s != null && s.size() == 2 && s.contains(1L) && s.contains(2L)),
                eq(99L)))
                .thenReturn(List.of());

        Map<Long, CommentViewerResponse> map = commentViewerStateService.resolveForComments(
                99L, List.of(1L, 2L), Map.of(1L, 99L, 2L, 10L));

        assertThat(map.get(1L).author()).isTrue();
        assertThat(map.get(2L).author()).isFalse();
    }
}
