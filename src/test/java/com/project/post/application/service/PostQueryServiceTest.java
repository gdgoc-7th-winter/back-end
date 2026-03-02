package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.domain.entity.Board;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostQueryService postQueryService;

    @Test
    @DisplayName("게시판이 없으면 목록 조회는 예외를 던진다")
    void getListThrowsWhenBoardMissing() {
        when(boardRepository.findByCodeAndActiveTrue("general")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postQueryService.getList("general", PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("상세 조회는 태그/첨부를 정렬 및 필터링한다")
    void getDetailMapsTagsAndAttachments() {
        Set<String> tagNames = new HashSet<>(List.of("spring", "java"));
        tagNames.add(null);

        Set<PostDetailQueryResult.AttachmentDto> attachments = new HashSet<>();
        attachments.add(new PostDetailQueryResult.AttachmentDto(null, "skip", "text/plain", 10L, 1));
        attachments.add(new PostDetailQueryResult.AttachmentDto("url-2", "b.txt", "text/plain", 20L, 2));
        attachments.add(new PostDetailQueryResult.AttachmentDto("url-1", "a.txt", "text/plain", 5L, null));

        PostDetailQueryResult result = new PostDetailQueryResult(
                1L,
                "title",
                "content",
                "thumb",
                "author",
                10L,
                3,
                2,
                1,
                4,
                Instant.now(),
                Instant.now(),
                tagNames,
                attachments
        );

        when(postRepository.findPostDetail(1L)).thenReturn(Optional.of(result));

        PostDetailResponse response = postQueryService.getDetail(1L);

        assertThat(response.tagNames()).containsExactly("java", "spring");
        assertThat(response.attachments()).hasSize(2);
        assertThat(response.attachments().get(0).fileUrl()).isEqualTo("url-1");
        assertThat(response.attachments().get(0).sortOrder()).isEqualTo(0);
        assertThat(response.attachments().get(1).fileUrl()).isEqualTo("url-2");
        assertThat(response.attachments().get(1).sortOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("목록 조회는 레포지토리 결과를 그대로 반환한다")
    void getListReturnsRepositoryPage() {
        when(boardRepository.findByCodeAndActiveTrue("general")).thenReturn(Optional.of(Board.of("general", "자유게시판")));

        Page<PostListResponse> expected = new PageImpl<>(Objects.requireNonNull(List.of(
                new PostListResponse(1L, "t", "thumb", "nick", 0, 0, 0, Instant.now())
        )));
        when(postRepository.findPostList("general", PageRequest.of(0, 10))).thenReturn(expected);

        Page<PostListResponse> result = postQueryService.getList("general", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).postId()).isEqualTo(1L);
    }
}
