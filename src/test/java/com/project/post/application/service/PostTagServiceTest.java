package com.project.post.application.service;

import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.Tag;
import com.project.post.application.service.impl.PostTagServiceImpl;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostTagServiceTest {

    @Mock
    private TagCreationService tagCreationService;

    @InjectMocks
    private PostTagServiceImpl postTagService;

    @Test
    @DisplayName("태그 입력이 null이면 아무 동작도 하지 않는다")
    void replaceTagsDoesNothingWhenNull() {
        postTagService.replaceTags(buildPost(1L), null);

        verifyNoInteractions(tagCreationService);
    }

    @Test
    @DisplayName("태그는 중복/공백을 제거하고 저장한다")
    void replaceTagsDedupesAndSaves() {
        Post post = buildPost(1L);

        when(tagCreationService.getOrCreate("java")).thenReturn(new Tag("java"));
        when(tagCreationService.getOrCreate("spring")).thenReturn(new Tag("spring"));

        postTagService.replaceTags(post, Arrays.asList(" java ", "spring", "java", "", null));

        verify(tagCreationService).getOrCreate("java");
        verify(tagCreationService).getOrCreate("spring");
        assertThat(post.getPostTags())
                .extracting(postTag -> postTag.getTag().getName())
                .containsExactlyInAnyOrder("java", "spring");
    }

    private static @NonNull Post buildPost(Long id) {
        User user = User.builder().email("user@test.com").password("pw").nickname("testuser").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Board board = Board.of("GENERAL", "자유/정보 게시판");
        ReflectionTestUtils.setField(Objects.requireNonNull(board), "id", 10L);
        return Objects.requireNonNull(Post.builder()
                .id(id)
                .board(board)
                .author(user)
                .title("title")
                .content("content")
                .build());
    }
}
