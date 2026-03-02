package com.project.post.application.service;

import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostTag;
import com.project.post.domain.entity.Tag;
import com.project.post.domain.repository.PostTagRepository;
import com.project.post.domain.repository.TagRepository;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostTagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostTagRepository postTagRepository;

    @Mock
    private TagCreationService tagCreationService;

    @InjectMocks
    private PostTagService postTagService;

    @Test
    @DisplayName("태그 입력이 null이면 아무 동작도 하지 않는다")
    void replaceTagsDoesNothingWhenNull() {
        postTagService.replaceTags(buildPost(1L), null);

        verifyNoInteractions(tagRepository, postTagRepository);
    }

    @Test
    @DisplayName("태그는 중복/공백을 제거하고 저장한다")
    void replaceTagsDedupesAndSaves() {
        Post post = buildPost(1L);

        when(tagRepository.findByName("java")).thenReturn(Optional.of(new Tag("java")));
        when(tagRepository.findByName("spring")).thenReturn(Optional.empty());
        when(tagCreationService.createTag("spring")).thenReturn(new Tag("spring"));

        postTagService.replaceTags(post, Arrays.asList(" java ", "spring", "java", "", null));

        verify(postTagRepository).deleteByPostId(1L);
        verify(tagRepository, times(2)).findByName(any(String.class));
        verify(tagCreationService, times(1)).createTag("spring");
        ArgumentCaptor<PostTag> captor = ArgumentCaptor.forClass(PostTag.class);
        verify(postTagRepository, times(2)).save(captor.capture());
    }

    @Test
    @DisplayName("태그 생성 중 중복으로 실패하면 재조회한다")
    void replaceTagsRecoversFromDuplicateTag() {
        Post post = buildPost(1L);

        when(tagRepository.findByName("spring"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Tag("spring")));
        when(tagCreationService.createTag("spring"))
                .thenThrow(new DataIntegrityViolationException("duplicate tag"));

        postTagService.replaceTags(post, Arrays.asList("spring"));

        verify(postTagRepository).deleteByPostId(1L);
        verify(tagRepository, times(2)).findByName("spring");
        verify(tagCreationService, times(1)).createTag("spring");
        ArgumentCaptor<PostTag> captor = ArgumentCaptor.forClass(PostTag.class);
        verify(postTagRepository, times(1)).save(captor.capture());
    }

    private static @NonNull Post buildPost(Long id) {
        User user = new User("user@test.com", "pw");
        ReflectionTestUtils.setField(user, "id", 1L);
        Board board = Board.of("general", "자유게시판");
        ReflectionTestUtils.setField(java.util.Objects.requireNonNull(board), "id", 10L);
        return Objects.requireNonNull(Post.builder()
                .id(id)
                .board(board)
                .author(user)
                .title("title")
                .content("content")
                .build());
    }
}
