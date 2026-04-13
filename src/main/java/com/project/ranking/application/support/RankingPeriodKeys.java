package com.project.ranking.application.support;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.regex.Pattern;

public final class RankingPeriodKeys {

    public static final String ALL_TIME_KEY = "ALL";
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final WeekFields ISO_WEEK = WeekFields.of(DayOfWeek.MONDAY, 4);
    private static final Pattern WEEK_KEY = Pattern.compile("^\\d{4}-W\\d{2}$");
    private static final Pattern MONTH_KEY = Pattern.compile("^\\d{4}-\\d{2}$");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("uuuu-MM");

    private RankingPeriodKeys() {
    }

    public static LocalDate todaySeoul() {
        return LocalDate.now(SEOUL);
    }

    public static LocalDate lastCompletedWeekMonday(LocalDate todaySeoul) {
        LocalDate thisWeekMonday = todaySeoul.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return thisWeekMonday.minusWeeks(1);
    }

    public static String formatWeekKey(LocalDate weekMonday) {
        int weekBasedYear = weekMonday.get(ISO_WEEK.weekBasedYear());
        int week = weekMonday.get(ISO_WEEK.weekOfWeekBasedYear());
        return String.format(Locale.ROOT, "%d-W%02d", weekBasedYear, week);
    }

    public static String defaultWeeklyPeriodKey(LocalDate todaySeoul) {
        return formatWeekKey(lastCompletedWeekMonday(todaySeoul));
    }

    public static String defaultMonthlyPeriodKey(LocalDate todaySeoul) {
        YearMonth ym = YearMonth.from(todaySeoul).minusMonths(1);
        return ym.format(MONTH_FMT);
    }

    public static LocalDate parseWeekMondayOrThrow(String periodKey) {
        if (!WEEK_KEY.matcher(periodKey).matches()) {
            throw new IllegalArgumentException("WEEKLY period_key은 YYYY-Www 형식이어야 합니다: " + periodKey);
        }
        int y = Integer.parseInt(periodKey.substring(0, 4));
        int w = Integer.parseInt(periodKey.substring(6));
        LocalDate anchor = LocalDate.of(y, 1, 4);
        return anchor
                .with(ISO_WEEK.weekOfWeekBasedYear(), w)
                .with(DayOfWeek.MONDAY);
    }

    public static YearMonth parseYearMonthOrThrow(String periodKey) {
        if (!MONTH_KEY.matcher(periodKey).matches()) {
            throw new IllegalArgumentException("MONTHLY period_key은 yyyy-MM 형식이어야 합니다: " + periodKey);
        }
        try {
            return YearMonth.parse(periodKey, MONTH_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("MONTHLY period_key이 유효하지 않습니다: " + periodKey, e);
        }
    }

    public static Instant weekStartInclusive(LocalDate weekMonday) {
        return weekMonday.atStartOfDay(SEOUL).toInstant();
    }

    public static Instant weekEndExclusive(LocalDate weekMonday) {
        return weekMonday.plusWeeks(1).atStartOfDay(SEOUL).toInstant();
    }

    public static Instant monthStartInclusive(YearMonth ym) {
        return ym.atDay(1).atStartOfDay(SEOUL).toInstant();
    }

    public static Instant monthEndExclusive(YearMonth ym) {
        return ym.plusMonths(1).atDay(1).atStartOfDay(SEOUL).toInstant();
    }
}
