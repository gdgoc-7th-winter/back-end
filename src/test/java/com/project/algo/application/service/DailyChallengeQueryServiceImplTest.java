package com.project.algo.application.service;

import com.project.algo.application.dto.DailyChallengeDetailResponse;
import com.project.algo.application.service.impl.DailyChallengeQueryServiceImpl;
import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.entity.DailyMVP;
import com.project.algo.domain.enums.CodingTestSite;
import com.project.algo.domain.repository.DailyChallengeRepository;
import com.project.algo.domain.repository.DailyMVPRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyChallengeQueryServiceImplTest {

    @Mock
    private DailyChallengeRepository dailyChallengeRepository;
    @Mock
    private DailyMVPRepository dailyMVPRepository;

    @InjectMocks
    private DailyChallengeQueryServiceImpl dailyChallengeQueryService;

    private User author;
    private DailyChallenge challenge;

    @BeforeEach
    void setUp() {
        author = User.builder().email("author@test.com").password("pw").nickname("author").build();
        ReflectionTestUtils.setField(author, "id", 1L);

        challenge = DailyChallenge.builder()
                .author(author)
                .title("두 수의 합")
                .sourceSite(CodingTestSite.BAEKJOON)
                .problemNumber("1000")
                .originalUrl("https://example.com")
                .build();
        ReflectionTestUtils.setField(challenge, "id", 10L);
    }

    @Test
    @DisplayName("존재하지 않는 문제 조회 시 RESOURCE_NOT_FOUND를 던진다")
    void getDetailThrowsWhenChallengeNotFound() {
        when(dailyChallengeRepository.findWithDetailById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyChallengeQueryService.getDetail(10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(dailyMVPRepository, never()).findByDailyChallengeIdOrderByRankAsc(any());
    }

    @Test
    @DisplayName("상세 조회 시 findWithDetailById를 사용한다")
    void getDetailCallsFindWithDetailById() {
        when(dailyChallengeRepository.findWithDetailById(10L)).thenReturn(Optional.of(challenge));
        when(dailyMVPRepository.findByDailyChallengeIdOrderByRankAsc(10L)).thenReturn(List.of());

        dailyChallengeQueryService.getDetail(10L);

        verify(dailyChallengeRepository).findWithDetailById(10L);
        verify(dailyChallengeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("상세 조회 시 MVP 정보가 응답에 포함된다")
    void getDetailIncludesMvps() {
        when(dailyChallengeRepository.findWithDetailById(10L)).thenReturn(Optional.of(challenge));

        User mvpUser = User.builder().email("mvp@test.com").password("pw").nickname("mvp1").build();
        ReflectionTestUtils.setField(mvpUser, "id", 99L);
        DailyMVP mvp = DailyMVP.of(challenge, mvpUser, 1, 10L, LocalDate.of(2024, 1, 1));
        when(dailyMVPRepository.findByDailyChallengeIdOrderByRankAsc(10L)).thenReturn(List.of(mvp));

        DailyChallengeDetailResponse response = dailyChallengeQueryService.getDetail(10L);

        assertThat(response.challengeId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("두 수의 합");
        assertThat(response.authorNickname()).isEqualTo("author");
        assertThat(response.mvps()).hasSize(1);
        assertThat(response.mvps().get(0).rank()).isEqualTo(1);
        assertThat(response.mvps().get(0).nickname()).isEqualTo("mvp1");
        assertThat(response.mvps().get(0).likeCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("MVP가 없는 경우 빈 목록을 반환한다")
    void getDetailWithNoMvps() {
        when(dailyChallengeRepository.findWithDetailById(10L)).thenReturn(Optional.of(challenge));
        when(dailyMVPRepository.findByDailyChallengeIdOrderByRankAsc(10L)).thenReturn(List.of());

        DailyChallengeDetailResponse response = dailyChallengeQueryService.getDetail(10L);

        assertThat(response.mvps()).isEmpty();
    }
}
