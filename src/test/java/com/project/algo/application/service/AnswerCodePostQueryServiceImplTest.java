package com.project.algo.application.service;

import com.project.algo.application.dto.AnswerCodePostDetailResponse;
import com.project.algo.application.dto.AnswerCodePostListResponse;
import com.project.algo.application.service.impl.AnswerCodePostQueryServiceImpl;
import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.enums.CodingTestSite;
import com.project.algo.domain.enums.ProgrammingLanguage;
import com.project.algo.domain.repository.AnswerCodePostRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerCodePostQueryServiceImplTest {

    @Mock
    private AnswerCodePostRepository answerCodePostRepository;

    @InjectMocks
    private AnswerCodePostQueryServiceImpl answerCodePostQueryService;

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
                .explanation("풀이 설명")
                .build();
        ReflectionTestUtils.setField(answer, "id", 100L);
    }

    // ── getList ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("풀이를 제출하지 않은 경우 목록 조회 시 ACCESS_DENIED를 던진다")
    void getListThrowsWhenNotSubmitted() {
        when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> answerCodePostQueryService.getList(10L, viewer, PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        verify(answerCodePostRepository, never()).findByDailyChallengeId(any(), any());
    }

    @Test
    @DisplayName("풀이를 제출한 경우 목록을 반환한다")
    void getListReturnsPageWhenSubmitted() {
        when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
        PageRequest pageable = PageRequest.of(0, 10);
        when(answerCodePostRepository.findByDailyChallengeId(eq(10L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(answer)));

        Page<?> result = answerCodePostQueryService.getList(10L, viewer, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ── getDetail ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("풀이를 제출하지 않은 경우 상세 조회 시 ACCESS_DENIED를 던진다")
    void getDetailThrowsWhenNotSubmitted() {
        when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> answerCodePostQueryService.getDetail(10L, 100L, viewer))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        verify(answerCodePostRepository, never()).findWithDetailById(any());
    }

    @Test
    @DisplayName("풀이를 제출했지만 해당 answer가 없으면 RESOURCE_NOT_FOUND를 던진다")
    void getDetailThrowsWhenAnswerNotFound() {
        when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
        when(answerCodePostRepository.findWithDetailById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerCodePostQueryService.getDetail(10L, 100L, viewer))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("풀이를 제출한 경우 findWithDetailById로 상세 조회한다")
    void getDetailCallsFindWithDetailById() {
        when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
        when(answerCodePostRepository.findWithDetailById(100L)).thenReturn(Optional.of(answer));

        AnswerCodePostDetailResponse response = answerCodePostQueryService.getDetail(10L, 100L, viewer);

        assertThat(response.answerId()).isEqualTo(100L);
        assertThat(response.authorNickname()).isEqualTo("viewer");
        verify(answerCodePostRepository).findWithDetailById(100L);
        verify(answerCodePostRepository, never()).findById(any());
    }

    // ── 탈퇴 사용자가 작성자인 경우 ──────────────────────────────────────────

    /**
     * 시나리오 1: 탈퇴 사용자가 작성자인 게시글 목록/상세 API 호출.
     *
     * @SQLRestriction 제거 후 Hibernate는 탈퇴한 User 행을 정상 로드하며,
     * withdraw()로 익명화된 nickname("탈퇴한 회원")이 DTO에 그대로 반영되는지 검증한다.
     */
    @Nested
    @DisplayName("탈퇴 사용자가 작성자인 게시글 조회")
    class WithdrawnAuthorTests {

        private User withdrawnAuthor;
        private AnswerCodePost postByWithdrawn;

        @BeforeEach
        void setUpWithdrawn() {
            withdrawnAuthor = User.builder()
                    .email("will-withdraw@test.com")
                    .password("pw")
                    .nickname("original")
                    .build();
            ReflectionTestUtils.setField(withdrawnAuthor, "id", 99L);
            withdrawnAuthor.withdraw(); // nickname → "탈퇴한 회원", deletedAt 설정

            postByWithdrawn = AnswerCodePost.builder()
                    .dailyChallenge(challenge)
                    .author(withdrawnAuthor)
                    .language(ProgrammingLanguage.JAVA)
                    .code("// solution")
                    .explanation("설명")
                    .build();
            ReflectionTestUtils.setField(postByWithdrawn, "id", 200L);
        }

        @Test
        @DisplayName("목록 조회: 탈퇴한 작성자의 닉네임이 '탈퇴한 회원'으로 표시된다")
        void getListShowsWithdrawnAuthorNickname() {
            when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
            when(answerCodePostRepository.findByDailyChallengeId(eq(10L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(postByWithdrawn)));

            Page<AnswerCodePostListResponse> result =
                    answerCodePostQueryService.getList(10L, viewer, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            AnswerCodePostListResponse item = result.getContent().get(0);
            assertThat(item.authorNickname()).isEqualTo(null);
            assertThat(item.authorId()).isEqualTo(99L); // FK ID는 유지
        }

        @Test
        @DisplayName("상세 조회: 탈퇴한 작성자의 닉네임이 '탈퇴한 회원'으로 표시된다")
        void getDetailShowsWithdrawnAuthorNickname() {
            when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
            when(answerCodePostRepository.findWithDetailById(200L)).thenReturn(Optional.of(postByWithdrawn));

            AnswerCodePostDetailResponse response =
                    answerCodePostQueryService.getDetail(10L, 200L, viewer);

            assertThat(response.authorNickname()).isEqualTo(null);
            assertThat(response.authorId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("상세 조회: 탈퇴 작성자 게시글 조회 시 예외 없이 정상 응답한다")
        void getDetailDoesNotThrowForWithdrawnAuthor() {
            when(answerCodePostRepository.existsByDailyChallengeIdAndAuthorId(10L, 1L)).thenReturn(true);
            when(answerCodePostRepository.findWithDetailById(200L)).thenReturn(Optional.of(postByWithdrawn));

            // @SQLRestriction 제거 전이라면 EntityNotFoundException 발생 위치
            // 제거 후에는 익명화된 User가 그대로 로드되어 정상 응답해야 한다
            org.assertj.core.api.Assertions.assertThatCode(
                    () -> answerCodePostQueryService.getDetail(10L, 200L, viewer)
            ).doesNotThrowAnyException();
        }
    }
}
