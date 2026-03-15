package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LecturePost.LecturePostCreateRequest;
import com.project.post.application.dto.LecturePost.LecturePostUpdateRequest;
import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PostUpdateRequest;
import com.project.post.application.service.impl.LecturePost.LecturePostCommandServiceImpl;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.LecturePost;
import com.project.post.domain.entity.Post;
import com.project.post.domain.enums.Campus;
import com.project.post.domain.repository.LecturePostRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LecturePostCommandServiceTest {

    @Mock
    private PostCommandService postCommandService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private LecturePostRepository lecturePostRepository;

    @InjectMocks
    private LecturePostCommandServiceImpl lecturePostCommandService;

    @Test
    @DisplayName("강의/수업 게시판이 없으면 생성 시 예외를 던진다")
    void createThrowsWhenBoardMissing() {
        when(postCommandService.create(eq("LECTURE"), any(PostCreateRequest.class), any(User.class)))
                .thenThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시판을 찾을 수 없습니다."));

        LecturePostCreateRequest request = new LecturePostCreateRequest(
                "제목", "본문", null, "컴퓨터공학과", Campus.SEOUL, null, null);

        assertThatThrownBy(() -> lecturePostCommandService.create(request, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(lecturePostRepository, never()).save(any());
    }

    @Test
    @DisplayName("강의/수업 게시글을 정상적으로 생성한다")
    void createSavesLecturePost() {
        Board board = Board.of("LECTURE", "강의/수업 게시판");
        ReflectionTestUtils.setField(board, "id", 10L);
        User author = buildUser(1L);
        Post post = buildPost(1L, board, author);

        when(postCommandService.create(eq("LECTURE"), any(PostCreateRequest.class), eq(author))).thenReturn(1L);
        when(postRepository.findActiveById(1L)).thenReturn(Optional.of(post));

        LecturePostCreateRequest request = new LecturePostCreateRequest(
                "알고리즘 과제 질문", "동적 프로그래밍 문제입니다.",
                null, "컴퓨터공학과", Campus.SEOUL,
                List.of("알고리즘", "과제"), null);

        Long result = lecturePostCommandService.create(request, author);

        assertThat(result).isEqualTo(1L);

        ArgumentCaptor<PostCreateRequest> postCreateCaptor = ArgumentCaptor.forClass(PostCreateRequest.class);
        verify(postCommandService).create(eq("LECTURE"), postCreateCaptor.capture(), eq(author));
        assertThat(postCreateCaptor.getValue().title()).isEqualTo("알고리즘 과제 질문");
        assertThat(postCreateCaptor.getValue().tagNames()).containsExactly("알고리즘", "과제");

        ArgumentCaptor<LecturePost> lectureCaptor = ArgumentCaptor.forClass(LecturePost.class);
        verify(lecturePostRepository).save(lectureCaptor.capture());
        assertThat(lectureCaptor.getValue().getDepartment()).isEqualTo("컴퓨터공학과");
        assertThat(lectureCaptor.getValue().getCampus()).isEqualTo(Campus.SEOUL);
    }

    @Test
    @DisplayName("강의/수업 게시글이 없으면 수정 시 예외를 던진다")
    void updateThrowsWhenLecturePostMissing() {
        when(lecturePostRepository.findActiveById(1L)).thenReturn(Optional.empty());

        LecturePostUpdateRequest request = new LecturePostUpdateRequest(
                "수정 제목", null, null, null, null, null, null);

        assertThatThrownBy(() -> lecturePostCommandService.update(1L, request, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("작성자가 아니면 수정 시 예외를 던진다")
    void updateThrowsWhenNotAuthor() {
        User author = buildUser(1L);
        User other = buildUser(2L);
        Board board = Board.of("LECTURE", "강의/수업 게시판");
        Post post = buildPost(1L, board, author);
        LecturePost lecturePost = buildLecturePost(post);

        when(lecturePostRepository.findActiveById(1L)).thenReturn(Optional.of(lecturePost));

        LecturePostUpdateRequest request = new LecturePostUpdateRequest(
                "수정 제목", null, null, null, null, null, null);

        assertThatThrownBy(() -> lecturePostCommandService.update(1L, request, other))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        assertThat(lecturePost.isDeleted()).isFalse();
        assertThat(post.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("수정 시 학과와 캠퍼스도 갱신하고 PostCommandService에 공통 로직을 위임한다")
    void updateModifiesLecturePost() {
        User author = buildUser(1L);
        Board board = Board.of("LECTURE", "강의/수업 게시판");
        Post post = buildPost(1L, board, author);
        LecturePost lecturePost = buildLecturePost(post);

        when(lecturePostRepository.findActiveById(1L)).thenReturn(Optional.of(lecturePost));

        LecturePostUpdateRequest request = new LecturePostUpdateRequest(
                "수정 제목", "수정 본문", null,
                "경영학과", Campus.GLOBAL,
                List.of("경영"), null);

        lecturePostCommandService.update(1L, request, author);

        assertThat(lecturePost.getDepartment()).isEqualTo("경영학과");
        assertThat(lecturePost.getCampus()).isEqualTo(Campus.GLOBAL);

        ArgumentCaptor<PostUpdateRequest> postUpdateCaptor = ArgumentCaptor.forClass(PostUpdateRequest.class);
        verify(postCommandService).update(eq(1L), postUpdateCaptor.capture(), eq(author));
        assertThat(postUpdateCaptor.getValue().title()).isEqualTo("수정 제목");
        assertThat(postUpdateCaptor.getValue().content()).isEqualTo("수정 본문");
        assertThat(postUpdateCaptor.getValue().tagNames()).containsExactly("경영");
    }

    @Test
    @DisplayName("강의/수업 게시글이 없으면 삭제 시 예외를 던진다")
    void deleteThrowsWhenLecturePostMissing() {
        when(lecturePostRepository.findActiveById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lecturePostCommandService.delete(1L, buildUser(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("작성자가 아니면 삭제 시 예외를 던진다")
    void deleteThrowsWhenNotAuthor() {
        User author = buildUser(1L);
        User other = buildUser(2L);
        Board board = Board.of("LECTURE", "강의/수업 게시판");
        Post post = buildPost(1L, board, author);
        LecturePost lecturePost = buildLecturePost(post);

        when(lecturePostRepository.findActiveById(1L)).thenReturn(Optional.of(lecturePost));

        assertThatThrownBy(() -> lecturePostCommandService.delete(1L, other))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        assertThat(lecturePost.isDeleted()).isFalse();
        assertThat(post.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("삭제 시 LecturePost와 Post 모두 소프트 삭제한다")
    void deleteSoftDeletesBoth() {
        User author = buildUser(1L);
        Board board = Board.of("LECTURE", "강의/수업 게시판");
        Post post = buildPost(1L, board, author);
        LecturePost lecturePost = buildLecturePost(post);

        when(lecturePostRepository.findActiveById(1L)).thenReturn(Optional.of(lecturePost));

        lecturePostCommandService.delete(1L, author);

        assertThat(lecturePost.isDeleted()).isTrue();
        verify(postCommandService).softDelete(1L, author);
    }

    private static User buildUser(Long id) {
        User user = new User("user@test.com", "pw");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Post buildPost(Long id, Board board, User author) {
        return Post.builder()
                .id(id)
                .board(board)
                .author(author)
                .title("원래 제목")
                .content("원래 본문")
                .build();
    }

    private static LecturePost buildLecturePost(Post post) {
        return LecturePost.builder()
                .post(post)
                .department("컴퓨터공학과")
                .campus(Campus.SEOUL)
                .build();
    }
}
