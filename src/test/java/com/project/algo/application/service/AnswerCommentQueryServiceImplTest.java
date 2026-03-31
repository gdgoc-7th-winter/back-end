package com.project.algo.application.service;

import com.project.algo.application.dto.AnswerCommentResponse;
import com.project.algo.application.service.impl.AnswerCommentQueryServiceImpl;
import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.entity.AnswerComment;
import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.enums.CodingTestSite;
import com.project.algo.domain.enums.ProgrammingLanguage;
import com.project.algo.domain.repository.AnswerCodePostRepository;
import com.project.algo.domain.repository.AnswerCommentRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerCommentQueryServiceImplTest {

    @Mock
    private AnswerCommentRepository answerCommentRepository;
    @Mock
    private AnswerCodePostRepository answerCodePostRepository;

    @InjectMocks
    private AnswerCommentQueryServiceImpl answerCommentQueryService;

    private User viewer;
    private DailyChallenge challenge;
    private AnswerCodePost answer;

    @BeforeEach
    void setUp() {
        viewer = User.builder().email("viewer@test.com").password("pw").nickname("viewer").build();
        ReflectionTestUtils.setField(viewer, "id", 1L);

        challenge = DailyChallenge.builder()
                .author(viewer)
                .title("문제 제목")
                .sourceSite(CodingTestSite.BAEKJOON)
                .problemNumber("1000")
                .originalUrl("https://example.com")
                .build();
        ReflectionTestUtils.setField(challenge, "id", 10L);

        answer = AnswerCodePost.builder()
                .dailyChallenge(challenge)
                .author(viewer)
                .language(ProgrammingLanguage.JAVA)
                .code("// solution")
                .explanation("설명")
                .build();
        ReflectionTestUtils.setField(answer, "id", 100L);
    }

    // ── 접근 제어 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("풀이를 제출하지 않은 경우 코멘트 목록 조회 시 ACCESS_DENIED를 던진다")
    void getListThrowsWhenNotSubmitted() {
        when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() ->
                answerCommentQueryService.getList(10L, 100L, viewer, PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        verify(answerCommentRepository, never()).findByAnswerCodePostId(any(), any());
    }

    @Test
    @DisplayName("풀이를 제출한 경우 코멘트 목록을 반환한다")
    void getListReturnsPageWhenSubmitted() {
        User commenter = User.builder().email("c@test.com").password("pw").nickname("commenter").build();
        ReflectionTestUtils.setField(commenter, "id", 2L);

        AnswerComment comment = AnswerComment.builder()
                .answerCodePost(answer)
                .author(commenter)
                .content("좋은 풀이네요")
                .build();
        ReflectionTestUtils.setField(comment, "id", 50L);

        when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
        when(answerCommentRepository.findByAnswerCodePostId(eq(100L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment)));

        Page<AnswerCommentResponse> result =
                answerCommentQueryService.getList(10L, 100L, viewer, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).authorNickname()).isEqualTo("commenter");
    }

    // ── 탈퇴 사용자가 댓글 작성자인 경우 ──────────────────────────────────────

    /**
     * 시나리오 1: 탈퇴 사용자가 댓글 작성자인 상태에서 목록 API 호출.
     *
     * @SQLRestriction 제거 전: AnswerComment.author를 lazy-load 시 deleted_at IS NULL 제약으로
     * EntityNotFoundException 발생 가능.
     * 제거 후: 익명화된 User 행이 정상 로드되어 authorNickname = "탈퇴한 회원"으로 응답해야 한다.
     */
    @Nested
    @DisplayName("탈퇴 사용자가 댓글 작성자인 경우")
    class WithdrawnCommenterTests {

        private User withdrawnCommenter;
        private AnswerComment commentByWithdrawn;

        @BeforeEach
        void setUpWithdrawn() {
            withdrawnCommenter = User.builder()
                    .email("will-withdraw@test.com")
                    .password("pw")
                    .nickname("original")
                    .build();
            ReflectionTestUtils.setField(withdrawnCommenter, "id", 77L);
            withdrawnCommenter.withdraw(); // nickname → "탈퇴한 회원"

            commentByWithdrawn = AnswerComment.builder()
                    .answerCodePost(answer)
                    .author(withdrawnCommenter)
                    .content("좋은 풀이네요")
                    .build();
            ReflectionTestUtils.setField(commentByWithdrawn, "id", 55L);
        }

        @Test
        @DisplayName("탈퇴한 댓글 작성자의 닉네임이 '탈퇴한 회원'으로 표시된다")
        void getListShowsWithdrawnCommenterNickname() {
            when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
            when(answerCommentRepository.findByAnswerCodePostId(eq(100L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(commentByWithdrawn)));

            Page<AnswerCommentResponse> result =
                    answerCommentQueryService.getList(10L, 100L, viewer, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            AnswerCommentResponse item = result.getContent().get(0);
            assertThat(item.authorNickname()).isEqualTo(null);
            assertThat(item.authorId()).isEqualTo(77L); // FK ID는 유지
            assertThat(item.deleted()).isFalse();        // 댓글 자체는 삭제되지 않음
        }

        @Test
        @DisplayName("탈퇴 댓글 작성자 조회 시 예외 없이 정상 응답한다")
        void getListDoesNotThrowForWithdrawnCommenter() {
            when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
            when(answerCommentRepository.findByAnswerCodePostId(eq(100L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(commentByWithdrawn)));

            assertThatCode(() ->
                    answerCommentQueryService.getList(10L, 100L, viewer, PageRequest.of(0, 10))
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("탈퇴 작성자와 활성 작성자 댓글이 혼재해도 각각 올바른 닉네임을 반환한다")
        void getListHandlesMixedAuthorStates() {
            User activeCommenter = User.builder()
                    .email("active@test.com").password("pw").nickname("활성유저").build();
            ReflectionTestUtils.setField(activeCommenter, "id", 88L);

            AnswerComment activeComment = AnswerComment.builder()
                    .answerCodePost(answer)
                    .author(activeCommenter)
                    .content("저도 이렇게 풀었어요")
                    .build();
            ReflectionTestUtils.setField(activeComment, "id", 56L);

            when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
            when(answerCommentRepository.findByAnswerCodePostId(eq(100L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(commentByWithdrawn, activeComment)));

            Page<AnswerCommentResponse> result =
                    answerCommentQueryService.getList(10L, 100L, viewer, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).authorNickname()).isEqualTo(null);
            assertThat(result.getContent().get(1).authorNickname()).isEqualTo("활성유저");
        }
    }
}
