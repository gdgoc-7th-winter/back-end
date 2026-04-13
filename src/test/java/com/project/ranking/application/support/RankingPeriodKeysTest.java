package com.project.ranking.application.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankingPeriodKeysTest {

    @Test
    @DisplayName("직전 완료 주 키는 ISO 주차 문자열이다")
    void weekKeyFormat() {
        LocalDate monday = LocalDate.of(2026, 3, 30);
        assertThat(RankingPeriodKeys.formatWeekKey(monday)).matches("\\d{4}-W\\d{2}");
    }

    @Test
    @DisplayName("주 키 파싱 후 월요일이 일치한다")
    void weekKeyRoundTrip() {
        LocalDate monday = LocalDate.of(2026, 4, 6);
        String key = RankingPeriodKeys.formatWeekKey(monday);
        assertThat(RankingPeriodKeys.parseWeekMondayOrThrow(key)).isEqualTo(monday);
    }

    @Test
    @DisplayName("잘못된 WEEKLY 키는 예외")
    void invalidWeekKey() {
        assertThatThrownBy(() -> RankingPeriodKeys.parseWeekMondayOrThrow("2026-04"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("주차 0 또는 54 이상은 예외")
    void weekNumberOutOfRange() {
        assertThatThrownBy(() -> RankingPeriodKeys.parseWeekMondayOrThrow("2026-W00"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RankingPeriodKeys.parseWeekMondayOrThrow("2026-W54"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ISO 주차와 week-based year가 어긋나면 round-trip 실패(예: 1995-W53 → 1996-W01)")
    void weekKeyMustMatchNormalizedForm() {
        assertThatThrownBy(() -> RankingPeriodKeys.parseWeekMondayOrThrow("1995-W53"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정규 형식");
    }

    @Test
    @DisplayName("월 키 파싱")
    void monthKey() {
        assertThat(RankingPeriodKeys.parseYearMonthOrThrow("2026-03")).isEqualTo(YearMonth.of(2026, 3));
    }
}
