package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.domain.entity.Board;
import com.project.post.application.service.impl.PostQueryServiceImpl;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.repository.dto.PostSearchCondition;
import com.project.post.domain.enums.PostListSort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Mock
    private PostTagQueryService postTagQueryService;

    @InjectMocks
    private PostQueryServiceImpl postQueryService;

    @Test
    @DisplayName("게시판이 없으면 목록 조회는 예외를 던진다")
    void getListThrowsWhenBoardMissing() {
        when(boardRepository.findByCodeAndActiveTrue("GENERAL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postQueryService.getList("GENERAL", PageRequest.of(0, 10), null, null, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("삭제된 게시글은 상세 조회 시 RESOURCE_NOT_FOUND (findPostDetail은 deleted_at 제외)")
    void getDetailThrowsWhenPostDeletedOrMissing() {
        when(postRepository.findPostDetail(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postQueryService.getDetail(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("상세 조회는 태그/첨부를 정렬 및 필터링한다")
    void getDetailMapsTagsAndAttachments() {
        List<String> tagNames = new ArrayList<>(List.of("spring", "java"));
        tagNames.add(null);

        List<PostDetailQueryResult.AttachmentDto> attachments = new ArrayList<>();
        attachments.add(new PostDetailQueryResult.AttachmentDto(null, "skip", "text/plain", 10L, 1));
        attachments.add(new PostDetailQueryResult.AttachmentDto("url-1", "a.txt", "text/plain", 5L, null));
        attachments.add(new PostDetailQueryResult.AttachmentDto("url-2", "b.txt", "text/plain", 20L, 2));

        PostDetailQueryResult result = new PostDetailQueryResult(
                1L,
                "title",
                "content",
                "thumb",
                "author",
                10L,
                3L,
                2L,
                1L,
                4L,
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
    @DisplayName("목록 조회는 레포지토리 결과를 Response DTO로 변환하여 반환한다")
    void getListReturnsRepositoryPage() {
        when(boardRepository.findByCodeAndActiveTrue("GENERAL")).thenReturn(Optional.of(Board.of("GENERAL", "자유/정보 게시판")));

        Page<PostListQueryResult> queryPage = new PageImpl<>(Objects.requireNonNull(List.of(
                new PostListQueryResult(1L, "t", "thumb", "nick", 0, 0, 0, 0, Instant.now())
        )));
        PostSearchCondition condition = new PostSearchCondition(null, null, PostListSort.LATEST);
        Pageable pageable = PageRequest.of(0, 10);
<<<<<<< HEAD
        when(postRepository.findPostList("general", pageable, condition)).thenReturn(queryPage);
        Post post = Post.builder()
                .id(1L)
                .board(Board.of("general", "자유게시판"))
                .author(new com.project.user.domain.entity.User("user@test.com", "pw", "testuser1"))
                .title("t")
                .content("content")
                .build();
        when(postTagRepository.findByPostIdIn(List.of(1L))).thenReturn(List.of(
                new PostTag(post, new Tag("java"))
        ));
=======
        when(postRepository.findPostList("GENERAL", pageable, condition)).thenReturn(queryPage);
        when(postTagQueryService.getTagNamesByPostIds(List.of(1L))).thenReturn(
                java.util.Map.of(1L, List.of("java")));
>>>>>>> 7250d19968757353bd05de6fc6fd3c089abe8d15

        Page<PostListResponse> result = postQueryService.getList("GENERAL", PageRequest.of(0, 10), null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).postId()).isEqualTo(1L);
    }
}
