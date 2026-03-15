package com.project.post.application.service;

import com.project.post.application.service.impl.PostTagQueryServiceImpl;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostTag;
import com.project.post.domain.entity.Tag;
import com.project.post.domain.repository.PostTagRepository;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostTagQueryServiceTest {

    @Mock
    private PostTagRepository postTagRepository;

    @InjectMocks
    private PostTagQueryServiceImpl postTagQueryService;

    @Test
    @DisplayName("postIds가 비어 있으면 빈 Map을 반환한다")
    void getTagNamesByPostIdsReturnsEmptyWhenNoPostIds() {
        Map<Long, List<String>> result = postTagQueryService.getTagNamesByPostIds(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("postId별 태그명 매핑을 반환한다")
    void getTagNamesByPostIdsReturnsTagNamesByPostId() {
        Post post1 = buildPost(1L);
        Post post2 = buildPost(2L);
        List<PostTag> postTags = List.of(
                new PostTag(post1, new Tag("java")),
                new PostTag(post1, new Tag("spring")),
                new PostTag(post2, new Tag("kotlin"))
        );

        when(postTagRepository.findByPostIdIn(List.of(1L, 2L))).thenReturn(postTags);

        Map<Long, List<String>> result = postTagQueryService.getTagNamesByPostIds(List.of(1L, 2L));

        assertThat(result).hasSize(2);
        assertThat(result.get(1L)).containsExactly("java", "spring");
        assertThat(result.get(2L)).containsExactly("kotlin");
    }

    @Test
    @DisplayName("태그명은 정렬·중복 제거되어 반환된다")
    void getTagNamesByPostIdsDedupesAndSorts() {
        Post post = buildPost(1L);
        List<PostTag> postTags = List.of(
                new PostTag(post, new Tag("spring")),
                new PostTag(post, new Tag("java")),
                new PostTag(post, new Tag("java"))
        );

        when(postTagRepository.findByPostIdIn(List.of(1L))).thenReturn(postTags);

        Map<Long, List<String>> result = postTagQueryService.getTagNamesByPostIds(List.of(1L));

        assertThat(result.get(1L)).containsExactly("java", "spring");
    }

    private static Post buildPost(Long id) {
        User user = new User("user@test.com", "pw");
        ReflectionTestUtils.setField(user, "id", 1L);
        Board board = Board.of("GENERAL", "자유/정보 게시판");
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
