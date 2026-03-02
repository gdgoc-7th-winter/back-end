package com.project.post.application.service;

import com.project.post.application.dto.PostAttachmentRequest;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostAttachment;
import com.project.post.domain.repository.PostAttachmentRepository;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PostAttachmentServiceTest {

    @Mock
    private PostAttachmentRepository postAttachmentRepository;

    @InjectMocks
    private PostAttachmentService postAttachmentService;

    @Test
    @DisplayName("attachments가 null이면 아무 동작도 하지 않는다")
    void replaceAttachmentsDoesNothingWhenNull() {
        Post post = buildPost(1L);

        postAttachmentService.replaceAttachments(post, null);

        verify(postAttachmentRepository, never()).deleteByPostId(any());
        verify(postAttachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("attachments가 비어있으면 기존 첨부만 삭제한다")
    void replaceAttachmentsDeletesOnlyWhenEmpty() {
        Post post = buildPost(1L);

        postAttachmentService.replaceAttachments(post, List.of());

        verify(postAttachmentRepository).deleteByPostId(1L);
        verify(postAttachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("첨부파일을 삭제 후 새로 저장한다")
    void replaceAttachmentsDeletesAndSaves() {
        Post post = buildPost(1L);
        List<PostAttachmentRequest> attachments = List.of(
                new PostAttachmentRequest("url1", "a.txt", "text/plain", 10L, 0),
                new PostAttachmentRequest("url2", "b.txt", "text/plain", 20L, 1)
        );

        postAttachmentService.replaceAttachments(post, attachments);

        verify(postAttachmentRepository).deleteByPostId(1L);
        verify(postAttachmentRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("null 또는 fileUrl이 null인 항목은 건너뛴다")
    void replaceAttachmentsSkipsInvalidEntries() {
        Post post = buildPost(1L);
        List<PostAttachmentRequest> attachments = Arrays.asList(
                new PostAttachmentRequest("url1", "a.txt", "text/plain", 10L, 0),
                null,
                new PostAttachmentRequest(null, "skip.txt", "text/plain", 5L, 1)
        );

        postAttachmentService.replaceAttachments(post, attachments);

        verify(postAttachmentRepository).deleteByPostId(1L);
        verify(postAttachmentRepository, times(1)).save(any());

        ArgumentCaptor<PostAttachment> captor = ArgumentCaptor.forClass(PostAttachment.class);
        verify(postAttachmentRepository).save(captor.capture());
        assertThat(captor.getValue().getFileUrl()).isEqualTo("url1");
    }

    @Test
    @DisplayName("sortOrder가 음수면 순서대로 0부터 부여한다")
    void replaceAttachmentsUsesIncrementalOrderWhenSortOrderNegative() {
        Post post = buildPost(1L);
        List<PostAttachmentRequest> attachments = List.of(
                new PostAttachmentRequest("url1", "a.txt", "text/plain", 10L, -1),
                new PostAttachmentRequest("url2", "b.txt", "text/plain", 20L, -1)
        );

        postAttachmentService.replaceAttachments(post, attachments);

        verify(postAttachmentRepository, times(2)).save(any());
    }

    private static Post buildPost(Long id) {
        User user = new User("user@test.com", "pw");
        ReflectionTestUtils.setField(user, "id", 1L);
        Board board = Board.of("general", "자유게시판");
        ReflectionTestUtils.setField(board, "id", 10L);
        return Post.builder()
                .id(id)
                .board(board)
                .author(user)
                .title("title")
                .content("content")
                .build();
    }
}
