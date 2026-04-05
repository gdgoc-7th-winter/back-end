package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PostUpdateRequest;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.port.ContributionOutboxPort;
import com.project.post.application.service.impl.PostCommandServiceImpl;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import com.project.global.event.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostTagService postTagService;

    @Mock
    private PostAttachmentService postAttachmentService;

    @Mock
    private ContributionOutboxPort contributionOutboxPort;

    @InjectMocks
    private PostCommandServiceImpl postCommandService;

    @Test
    @DisplayName("게시판이 없으면 생성 시 예외를 던진다")
    void createThrowsWhenBoardMissing() {
        when(boardRepository.findByCodeAndActiveTrue("GENERAL")).thenReturn(Optional.empty());

        PostCreateRequest request = new PostCreateRequest("title", "content", null, null, null);

        assertThatThrownBy(() -> postCommandService.create("GENERAL", request, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글 생성 시 태그와 첨부파일을 저장한다")
    void createSavesPostWithTagsAndAttachments() {
        Board board = Board.of("GENERAL", "자유/정보 게시판");
        ReflectionTestUtils.setField(board, "id", 10L);
        User author = buildUser(1L);
        Post post = buildPost(1L, board, author);

        when(boardRepository.findByCodeAndActiveTrue("GENERAL")).thenReturn(Optional.of(board));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostCreateRequest request = new PostCreateRequest(
                "title", "content", "thumb",
                List.of("java", "spring"),
                List.of(new com.project.post.application.dto.PostAttachmentRequest("url", "a.txt", "text/plain", 10L, 0))
        );

        Post result = postCommandService.create("GENERAL", request, author);

        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getTitle()).isEqualTo("title");
        assertThat(postCaptor.getValue().getContent()).isEqualTo("content");

        verify(postTagService).replaceTags(post, List.of("java", "spring"));
        verify(postAttachmentService).replaceAttachments(post, request.attachments());
        verify(contributionOutboxPort)
                .append(
                        argThat(
                                (ActivityContext c) ->
                                        c.activityType() == ActivityType.POST_CREATED
                                                && c.subjectUserId() == 1L
                                                && c.referenceId() == 1L));
    }

    @Test
    @DisplayName("게시글이 없으면 수정 시 예외를 던진다")
    void updateThrowsWhenPostMissing() {
        when(postRepository.findActiveById(1L)).thenReturn(Optional.empty());

        PostUpdateRequest request = new PostUpdateRequest("title", "content", null, null, null);

        assertThatThrownBy(() -> postCommandService.update(1L, request, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("작성자가 아니면 수정 시 예외를 던진다")
    void updateThrowsWhenNotAuthor() {
        User author = buildUser(1L);
        User other = buildUser(2L);
        Post post = buildPost(1L, Board.of("GENERAL", "자유"), author);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest("title", "content", null, null, null);

        assertThatThrownBy(() -> postCommandService.update(1L, request, other))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        verify(postTagService, never()).replaceTags(any(), any());
    }

    @Test
    @DisplayName("수정 시 제목·본문·태그·첨부를 갱신한다")
    void updateModifiesPost() {
        User author = buildUser(1L);
        Board board = Board.of("GENERAL", "자유");
        Post post = buildPost(1L, board, author);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest(
                "new title", "new content", "new thumb",
                List.of("tag"),
                List.of()
        );

        postCommandService.update(1L, request, author);

        assertThat(post.getTitle()).isEqualTo("new title");
        assertThat(post.getContent()).isEqualTo("new content");
        assertThat(post.getThumbnailUrl()).isEqualTo("new thumb");
        verify(postTagService).replaceTags(post, List.of("tag"));
        verify(postAttachmentService).replaceAttachments(post, List.of());
    }

    @Test
    @DisplayName("게시글이 없으면 삭제 시 예외를 던진다")
    void softDeleteThrowsWhenPostMissing() {
        when(postRepository.findActiveById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postCommandService.softDelete(1L, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("작성자가 아니면 삭제 시 예외를 던진다")
    void softDeleteThrowsWhenNotAuthor() {
        User author = buildUser(1L);
        User other = buildUser(2L);
        Post post = buildPost(1L, Board.of("GENERAL", "자유"), author);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postCommandService.softDelete(1L, other))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        assertThat(post.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("삭제 시 소프트 삭제 처리한다")
    void softDeleteMarksAsDeleted() {
        User author = buildUser(1L);
        Post post = buildPost(1L, Board.of("GENERAL", "자유"), author);

        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));

        postCommandService.softDelete(1L, author);

        assertThat(post.isDeleted()).isTrue();
        verify(contributionOutboxPort)
                .append(
                        argThat(
                                (ActivityContext c) ->
                                        c.activityType() == ActivityType.POST_DELETED
                                                && c.subjectUserId() == 1L
                                                && c.referenceId() == 1L));
    }

    @Test
    @DisplayName("게시글이 없으면 조회수 증가 시 예외를 던진다")
    void increaseViewCountThrowsWhenPostMissing() {
        when(postRepository.incrementViewCount(1L)).thenReturn(0);

        assertThatThrownBy(() -> postCommandService.increaseViewCount(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("조회수 증가 시 incrementViewCount를 호출한다")
    void increaseViewCountIncrements() {
        when(postRepository.incrementViewCount(1L)).thenReturn(1);

        postCommandService.increaseViewCount(1L);

        verify(postRepository).incrementViewCount(1L);
    }

    private static User buildUser(Long id) {
        User user = User.builder().email("user@test.com").password("pw").nickname("testuser").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Post buildPost(Long id, Board board, User author) {
        return Post.builder()
                .id(id)
                .board(board)
                .author(author)
                .title("title")
                .content("content")
                .build();
    }
}
