package com.project.algo.application.service;

import com.project.algo.application.service.impl.MvpSelectionServiceImpl;
import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.entity.DailyMVP;
import com.project.algo.domain.enums.CodingTestSite;
import com.project.algo.domain.enums.ProgrammingLanguage;
import com.project.algo.domain.repository.AnswerCodePostRepository;
import com.project.algo.domain.repository.DailyChallengeRepository;
import com.project.algo.domain.repository.DailyMVPRepository;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MvpSelectionServiceImplTest {

    @Mock
    private DailyChallengeRepository dailyChallengeRepository;
    @Mock
    private AnswerCodePostRepository answerCodePostRepository;
    @Mock
    private DailyMVPRepository dailyMVPRepository;

    @InjectMocks
    private MvpSelectionServiceImpl mvpSelectionService;

    private static final LocalDate TARGET_DATE = LocalDate.of(2024, 1, 1);
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private User author;
    private DailyChallenge challenge;

    @BeforeEach
    void setUp() {
        author = User.builder().email("u@test.com").password("pw").nickname("tester").build();
        ReflectionTestUtils.setField(author, "id", 1L);

        challenge = DailyChallenge.builder()
                .author(author)
                .title("문제")
                .sourceSite(CodingTestSite.BAEKJOON)
                .problemNumber("1000")
                .originalUrl("https://example.com")
                .build();
        ReflectionTestUtils.setField(challenge, "id", 10L);
    }

    @Test
    @DisplayName("해당 날짜에 문제가 없으면 MVP 선정을 건너뛴다")
    void skipWhenNoChallenges() {
        Instant start = TARGET_DATE.atStartOfDay(SEOUL).toInstant();
        Instant end = TARGET_DATE.plusDays(1).atStartOfDay(SEOUL).toInstant();
        when(dailyChallengeRepository.findAllCreatedBetween(start, end)).thenReturn(List.of());

        mvpSelectionService.selectDailyMvps(TARGET_DATE);

        verify(answerCodePostRepository, never()).findTop3ForMvp(any(), any());
        verify(dailyMVPRepository, never()).save(any());
    }

    @Test
    @DisplayName("문제는 있지만 풀이가 없으면 MVP를 저장하지 않는다")
    void skipWhenNoAnswers() {
        Instant start = TARGET_DATE.atStartOfDay(SEOUL).toInstant();
        Instant end = TARGET_DATE.plusDays(1).atStartOfDay(SEOUL).toInstant();
        when(dailyChallengeRepository.findAllCreatedBetween(start, end)).thenReturn(List.of(challenge));
        when(answerCodePostRepository.findTop3ForMvp(eq(10L), any(Pageable.class))).thenReturn(List.of());

        mvpSelectionService.selectDailyMvps(TARGET_DATE);

        verify(dailyMVPRepository, never()).save(any());
    }

    @Test
    @DisplayName("상위 3개 풀이에 대해 rank 1~3으로 DailyMVP를 저장한다")
    void saveMvpsWithCorrectRanks() {
        Instant start = TARGET_DATE.atStartOfDay(SEOUL).toInstant();
        Instant end = TARGET_DATE.plusDays(1).atStartOfDay(SEOUL).toInstant();
        when(dailyChallengeRepository.findAllCreatedBetween(start, end)).thenReturn(List.of(challenge));

        List<AnswerCodePost> top3 = buildAnswers(3);
        when(answerCodePostRepository.findTop3ForMvp(eq(10L), any(Pageable.class))).thenReturn(top3);

        mvpSelectionService.selectDailyMvps(TARGET_DATE);

        ArgumentCaptor<DailyMVP> captor = ArgumentCaptor.forClass(DailyMVP.class);
        verify(dailyMVPRepository, times(3)).save(captor.capture());

        List<DailyMVP> saved = captor.getAllValues();
        assertThat(saved).extracting(DailyMVP::getRank).containsExactly(1, 2, 3);
        assertThat(saved).extracting(DailyMVP::getAwardedAt).containsOnly(TARGET_DATE);
    }

    @Test
    @DisplayName("MVP 재선정 시 기존 데이터를 먼저 삭제한다 (멱등성)")
    void deletesBeforeSaving() {
        Instant start = TARGET_DATE.atStartOfDay(SEOUL).toInstant();
        Instant end = TARGET_DATE.plusDays(1).atStartOfDay(SEOUL).toInstant();
        when(dailyChallengeRepository.findAllCreatedBetween(start, end)).thenReturn(List.of(challenge));
        when(answerCodePostRepository.findTop3ForMvp(eq(10L), any(Pageable.class))).thenReturn(buildAnswers(1));

        mvpSelectionService.selectDailyMvps(TARGET_DATE);

        verify(dailyMVPRepository).deleteByDailyChallengeIdAndAwardedAt(10L, TARGET_DATE);
    }

    @Test
    @DisplayName("풀이가 1개뿐이면 rank 1만 저장한다")
    void savesOnlyOneWhenSingleAnswer() {
        Instant start = TARGET_DATE.atStartOfDay(SEOUL).toInstant();
        Instant end = TARGET_DATE.plusDays(1).atStartOfDay(SEOUL).toInstant();
        when(dailyChallengeRepository.findAllCreatedBetween(start, end)).thenReturn(List.of(challenge));
        when(answerCodePostRepository.findTop3ForMvp(eq(10L), any(Pageable.class))).thenReturn(buildAnswers(1));

        mvpSelectionService.selectDailyMvps(TARGET_DATE);

        verify(dailyMVPRepository, times(1)).save(any(DailyMVP.class));
    }

    @Test
    @DisplayName("findTop3ForMvp 호출 시 Pageable(size=3)을 전달한다")
    void passesPageableWithCorrectSize() {
        Instant start = TARGET_DATE.atStartOfDay(SEOUL).toInstant();
        Instant end = TARGET_DATE.plusDays(1).atStartOfDay(SEOUL).toInstant();
        when(dailyChallengeRepository.findAllCreatedBetween(start, end)).thenReturn(List.of(challenge));
        when(answerCodePostRepository.findTop3ForMvp(eq(10L), any(Pageable.class))).thenReturn(List.of());

        mvpSelectionService.selectDailyMvps(TARGET_DATE);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(answerCodePostRepository).findTop3ForMvp(eq(10L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(3);
    }

    // ── 탈퇴 사용자가 MVP 후보인 경우 ────────────────────────────────────────

    /**
     * 시나리오 2: findTop3ForMvp 쿼리는 JOIN FETCH a.author로 User를 fetch한다.
     *
     * @SQLRestriction 제거 전: JOIN FETCH 대상이 deleted_at IS NULL 조건에 걸려
     * 탈퇴 사용자의 풀이가 MVP 후보에서 누락되거나 EntityNotFoundException 위험이 있었다.
     * 제거 후: 탈퇴 사용자 행도 JOIN 대상에 포함되어 MVP로 정상 저장되어야 한다.
     */
    @Test
    @DisplayName("탈퇴 사용자가 작성한 풀이도 MVP 후보에 포함되어 정상 저장된다 (JOIN FETCH 경로)")
    void mvpSelectionSavesWithdrawnAuthorSuccessfully() {
        // given
        Instant start = TARGET_DATE.atStartOfDay(SEOUL).toInstant();
        Instant end = TARGET_DATE.plusDays(1).atStartOfDay(SEOUL).toInstant();
        when(dailyChallengeRepository.findAllCreatedBetween(start, end)).thenReturn(List.of(challenge));

        User withdrawnAuthor = User.builder()
                .email("will-withdraw@test.com").password("pw").nickname("original").build();
        ReflectionTestUtils.setField(withdrawnAuthor, "id", 77L);
        withdrawnAuthor.withdraw(); // nickname → "탈퇴한 회원"

        AnswerCodePost postByWithdrawn = AnswerCodePost.builder()
                .dailyChallenge(challenge)
                .author(withdrawnAuthor)
                .language(ProgrammingLanguage.JAVA)
                .code("// code")
                .explanation("설명")
                .build();
        ReflectionTestUtils.setField(postByWithdrawn, "id", 300L);
        ReflectionTestUtils.setField(postByWithdrawn, "likeCount", 5L);

        when(answerCodePostRepository.findTop3ForMvp(eq(10L), any(Pageable.class)))
                .thenReturn(List.of(postByWithdrawn));

        // when
        mvpSelectionService.selectDailyMvps(TARGET_DATE);

        // then: 탈퇴 사용자도 MVP로 저장, 익명화된 닉네임 확인
        ArgumentCaptor<DailyMVP> captor = ArgumentCaptor.forClass(DailyMVP.class);
        verify(dailyMVPRepository).save(captor.capture());
        DailyMVP saved = captor.getValue();
        assertThat(saved.getRank()).isEqualTo(1);
        assertThat(saved.getUser().getNickname()).isEqualTo(null);
        assertThat(saved.getUser().getId()).isEqualTo(77L);
    }

    @Test
    @DisplayName("탈퇴 사용자와 활성 사용자가 혼재한 MVP 후보를 rank 순서대로 저장한다")
    void mvpSelectionHandlesMixedAuthorStates() {
        // given
        Instant start = TARGET_DATE.atStartOfDay(SEOUL).toInstant();
        Instant end = TARGET_DATE.plusDays(1).atStartOfDay(SEOUL).toInstant();
        when(dailyChallengeRepository.findAllCreatedBetween(start, end)).thenReturn(List.of(challenge));

        // rank 1: 탈퇴 사용자 (좋아요 10)
        User withdrawnAuthor = User.builder()
                .email("will-withdraw@test.com").password("pw").nickname("original").build();
        ReflectionTestUtils.setField(withdrawnAuthor, "id", 77L);
        withdrawnAuthor.withdraw();
        AnswerCodePost rank1 = AnswerCodePost.builder()
                .dailyChallenge(challenge).author(withdrawnAuthor)
                .language(ProgrammingLanguage.JAVA).code("c").explanation("e").build();
        ReflectionTestUtils.setField(rank1, "id", 301L);
        ReflectionTestUtils.setField(rank1, "likeCount", 10L);

        // rank 2: 활성 사용자 (좋아요 8)
        User activeAuthor = User.builder()
                .email("active@test.com").password("pw").nickname("활성유저").build();
        ReflectionTestUtils.setField(activeAuthor, "id", 88L);
        AnswerCodePost rank2 = AnswerCodePost.builder()
                .dailyChallenge(challenge).author(activeAuthor)
                .language(ProgrammingLanguage.JAVA).code("c").explanation("e").build();
        ReflectionTestUtils.setField(rank2, "id", 302L);
        ReflectionTestUtils.setField(rank2, "likeCount", 8L);

        when(answerCodePostRepository.findTop3ForMvp(eq(10L), any(Pageable.class)))
                .thenReturn(List.of(rank1, rank2));

        // when
        mvpSelectionService.selectDailyMvps(TARGET_DATE);

        // then
        ArgumentCaptor<DailyMVP> captor = ArgumentCaptor.forClass(DailyMVP.class);
        verify(dailyMVPRepository, times(2)).save(captor.capture());
        List<DailyMVP> saved = captor.getAllValues();
        assertThat(saved.get(0).getRank()).isEqualTo(1);
        assertThat(saved.get(0).getUser().getNickname()).isEqualTo(null);
        assertThat(saved.get(1).getRank()).isEqualTo(2);
        assertThat(saved.get(1).getUser().getNickname()).isEqualTo("활성유저");
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private List<AnswerCodePost> buildAnswers(int count) {
        return java.util.stream.IntStream.range(0, count).mapToObj(i -> {
            User u = User.builder().email("u" + i + "@test.com").password("pw").nickname("user" + i).build();
            ReflectionTestUtils.setField(u, "id", (long) (100 + i));

            AnswerCodePost answer = AnswerCodePost.builder()
                    .dailyChallenge(challenge)
                    .author(u)
                    .language(ProgrammingLanguage.JAVA)
                    .code("// code")
                    .explanation("설명")
                    .build();
            ReflectionTestUtils.setField(answer, "id", (long) (200 + i));
            ReflectionTestUtils.setField(answer, "likeCount", (long) (10 - i));
            return answer;
        }).toList();
    }
}
