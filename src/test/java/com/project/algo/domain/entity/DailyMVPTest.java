package com.project.algo.domain.entity;

import com.project.algo.domain.enums.CodingTestSite;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DailyMVPTest {

    private DailyChallenge challenge;
    private User user;
    private final LocalDate today = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        user = User.builder().email("test@test.com").password("pw").nickname("nick").build();
        ReflectionTestUtils.setField(user, "id", 1L);

        challenge = DailyChallenge.builder()
                .author(user)
                .title("두 수의 합")
                .sourceSite(CodingTestSite.BAEKJOON)
                .problemNumber("1000")
                .originalUrl("https://example.com")
                .build();
        ReflectionTestUtils.setField(challenge, "id", 10L);
    }

    @Test
    @DisplayName("유효한 입력으로 DailyMVP를 생성한다")
    void ofSuccess() {
        DailyMVP mvp = DailyMVP.of(challenge, user, 1, 5L, today);

        assertThat(mvp.getRank()).isEqualTo(1);
        assertThat(mvp.getLikeCount()).isEqualTo(5L);
        assertThat(mvp.getAwardedAt()).isEqualTo(today);
        assertThat(mvp.getUser()).isSameAs(user);
        assertThat(mvp.getDailyChallenge()).isSameAs(challenge);
    }

    @Test
    @DisplayName("likeCount가 0이어도 생성된다")
    void ofSuccessWithZeroLikeCount() {
        DailyMVP mvp = DailyMVP.of(challenge, user, 3, 0L, today);
        assertThat(mvp.getLikeCount()).isZero();
    }

    @Test
    @DisplayName("dailyChallenge가 null이면 NullPointerException을 던진다")
    void ofThrowsWhenChallengeIsNull() {
        assertThatThrownBy(() -> DailyMVP.of(null, user, 1, 0L, today))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("user가 null이면 NullPointerException을 던진다")
    void ofThrowsWhenUserIsNull() {
        assertThatThrownBy(() -> DailyMVP.of(challenge, null, 1, 0L, today))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("awardedAt이 null이면 NullPointerException을 던진다")
    void ofThrowsWhenAwardedAtIsNull() {
        assertThatThrownBy(() -> DailyMVP.of(challenge, user, 1, 0L, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("rank가 0이면 IllegalArgumentException을 던진다")
    void ofThrowsWhenRankIsZero() {
        assertThatThrownBy(() -> DailyMVP.of(challenge, user, 0, 0L, today))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rank는 1~3 사이여야 합니다");
    }

    @Test
    @DisplayName("rank가 4이면 IllegalArgumentException을 던진다")
    void ofThrowsWhenRankExceedsThree() {
        assertThatThrownBy(() -> DailyMVP.of(challenge, user, 4, 0L, today))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rank는 1~3 사이여야 합니다");
    }

    @Test
    @DisplayName("likeCount가 음수이면 IllegalArgumentException을 던진다")
    void ofThrowsWhenLikeCountIsNegative() {
        assertThatThrownBy(() -> DailyMVP.of(challenge, user, 1, -1L, today))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("likeCount는 0 이상이어야 합니다");
    }
}
