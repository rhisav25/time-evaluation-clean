package com.atoss.timeevaluation.service;

import com.atoss.timeevaluation.TestRules;
import com.atoss.timeevaluation.config.EvaluationProperties;
import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.model.*;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import static com.atoss.timeevaluation.TestRules.minutes;
import static org.junit.jupiter.api.Assertions.*;

class EvaluationServiceTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 19);

    private final EvaluationService service =
            new EvaluationService(TestRules.defaultProperties());

    // ---- helpers ------------------------------------------------------------

    /** Mon-Fri: 8 h expected, window 08:00-18:00. Sat/Sun: nothing expected. */
    private static List<DailyInformation> fullWeek() {
        return fullWeek(-1);
    }

    private static List<DailyInformation> fullWeek(int publicHolidayOffset) {
        return IntStream.range(0, 7)
                .mapToObj(i -> new DailyInformation(
                        MONDAY.plusDays(i),
                        i == publicHolidayOffset,
                        i < 5 ? 480 : 0,
                        i < 5 ? LocalTime.of(8, 0) : null,
                        i < 5 ? LocalTime.of(18, 0) : null))
                .toList();
    }

    private static AttendanceInterval work(int dayOffset, int fromHour, int toHour) {
        return new AttendanceInterval(
                MONDAY.plusDays(dayOffset), LocalTime.of(fromHour, 0), LocalTime.of(toHour, 0));
    }

    private static EvaluationRequest week(AttendanceInterval... intervals) {
        return new EvaluationRequest(MONDAY, MONDAY.plusDays(6), List.of(intervals), fullWeek());
    }

    /** Hours per weekday Mon..Sun, each worked from 08:00. */
    private static EvaluationRequest weekWithHours(int... hours) {
        return week(IntStream.range(0, hours.length)
                .filter(i -> hours[i] > 0)
                .mapToObj(i -> work(i, 8, 8 + hours[i]))
                .toArray(AttendanceInterval[]::new));
    }

    private static long total(EvaluationResponse response, String wageName) {
        return response.dailyResults().stream()
                .mapToLong(day -> minutes(day.wages(), wageName))
                .sum();
    }

    // ---- daily rules --------------------------------------------------------

    @Test
    void shouldEvaluateAttendanceAndDailyWages() {
        EvaluationRequest request = new EvaluationRequest(
                MONDAY,
                MONDAY,
                List.of(
                        new AttendanceInterval(MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                        new AttendanceInterval(MONDAY, LocalTime.of(13, 0), LocalTime.of(18, 0))
                ),
                List.of(new DailyInformation(
                        MONDAY, false, 480, LocalTime.of(9, 0), LocalTime.of(17, 0)))
        );

        List<DailyEvaluationResult> results = service.evaluateDaily(request);

        assertEquals(1, results.size());

        DailyEvaluationResult result = results.get(0);

        assertEquals(MONDAY, result.date());
        assertEquals(5, result.wages().size());

        assertEquals(540, result.wages().get(0).durationMinutes());
        assertEquals(60, result.wages().get(1).durationMinutes());
        assertEquals(120, result.wages().get(2).durationMinutes());
        assertEquals(0, result.wages().get(3).durationMinutes());
        assertEquals(0, result.wages().get(4).durationMinutes());
    }

    @Test
    void shouldIncludeDaysWithoutAttendance() {
        EvaluationRequest request = new EvaluationRequest(
                MONDAY,
                MONDAY,
                List.of(),
                List.of(new DailyInformation(
                        MONDAY, false, 480, LocalTime.of(9, 0), LocalTime.of(17, 0)))
        );

        List<DailyEvaluationResult> results = service.evaluateDaily(request);

        assertEquals(1, results.size());
        assertEquals(0, results.get(0).wages().get(0).durationMinutes());
        assertEquals(0, results.get(0).wages().get(1).durationMinutes());
    }

    @Test
    void shouldCountPublicHolidayWorkButNotSundayWorkWhenBothApply() {
        LocalDate sunday = LocalDate.of(2026, 10, 25);

        EvaluationRequest request = new EvaluationRequest(
                sunday,
                sunday,
                List.of(new AttendanceInterval(sunday, LocalTime.of(9, 0), LocalTime.of(13, 0))),
                List.of(new DailyInformation(
                        sunday, true, 480, LocalTime.of(9, 0), LocalTime.of(17, 0)))
        );

        List<Wage> wages = service.evaluateDaily(request).get(0).wages();

        assertEquals(0, minutes(wages, "sunday-work"));
        assertEquals(240, minutes(wages, "public-holiday-work"));
    }

    @Test
    void shouldCountSundayWorkOnOrdinarySunday() {
        EvaluationResponse response = service.evaluate(weekWithHours(0, 0, 0, 0, 0, 0, 5));

        List<Wage> sunday = response.dailyResults().get(6).wages();

        assertEquals(300, minutes(sunday, "sunday-work"));
        assertEquals(0, minutes(sunday, "public-holiday-work"));
    }

    // ---- scenario 5: daily / weekly overtime reconciliation -----------------

    @Test
    void shouldReconcileDailyAndWeeklyOvertimeForScenarioA() {
        // 10, 10, 8, 8, 6, 0, 0 = 42 h
        EvaluationResponse response = service.evaluate(weekWithHours(10, 10, 8, 8, 6, 0, 0));

        assertEquals(42 * 60, total(response, "attendance"));
        assertEquals(4 * 60, total(response, "daily-overtime"));
        assertEquals(1, response.weeklyResults().size());
        assertEquals(0, minutes(response.weeklyResults().get(0).wages(), "weekly-overtime"));
    }

    @Test
    void shouldReconcileDailyAndWeeklyOvertimeForScenarioB() {
        // 9, 9, 8, 8, 8, 4, 0 = 46 h
        EvaluationResponse response = service.evaluate(weekWithHours(9, 9, 8, 8, 8, 4, 0));

        assertEquals(46 * 60, total(response, "attendance"));
        assertEquals(2 * 60, total(response, "daily-overtime"));
        assertEquals(4 * 60, minutes(response.weeklyResults().get(0).wages(), "weekly-overtime"));
    }

    @Test
    void shouldOnlyExposeVisibleRulesInTheResponse() {
        EvaluationResponse response = service.evaluate(weekWithHours(9, 9, 8, 8, 8, 4, 0));

        List<Wage> weekly = response.weeklyResults().get(0).wages();

        assertEquals(1, weekly.size());
        assertEquals("weekly-overtime", weekly.get(0).name());
    }

    // ---- configurability ----------------------------------------------------

    @Test
    void shouldStillCalculateWeeklyOvertimeWhenAttendanceIsHidden() {
        // Previously, disabling the "attendance" wage silently made weekly overtime 0.
        List<RuleDefinition> rules = TestRules.defaultRules().stream()
                .map(rule -> rule.name().equals("attendance")
                        ? new RuleDefinition(rule.name(), rule.type(), rule.scope(), rule.input(),
                        rule.subtract(), rule.thresholdMinutes(), rule.days(),
                        rule.publicHoliday(), false)
                        : rule)
                .toList();

        EvaluationService hiddenAttendance =
                new EvaluationService(new EvaluationProperties(rules));

        EvaluationResponse response =
                hiddenAttendance.evaluate(weekWithHours(9, 9, 8, 8, 8, 4, 0));

        assertTrue(response.dailyResults().get(0).wages().stream()
                .noneMatch(wage -> wage.name().equals("attendance")));
        assertEquals(4 * 60, minutes(response.weeklyResults().get(0).wages(), "weekly-overtime"));
    }

    @Test
    void shouldApplyDifferentRulesWithoutCodeChanges() {
        // Overtime also on Saturdays, weekly threshold 38.5 h.
        List<DayOfWeek> mondayToSaturday = List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

        EvaluationService custom = new EvaluationService(new EvaluationProperties(
                TestRules.defaultRules(mondayToSaturday, 2310)));

        EvaluationResponse response = custom.evaluate(weekWithHours(9, 9, 8, 8, 8, 4, 0));

        // Mon/Tue 1 h each + Saturday 4 h (expected 0) = 6 h daily overtime
        assertEquals(6 * 60, total(response, "daily-overtime"));
        // weekly: 46 h - 38.5 h = 7.5 h, minus 6 h daily = 1.5 h
        assertEquals(90, minutes(response.weeklyResults().get(0).wages(), "weekly-overtime"));
    }

    @Test
    void shouldOmitWeeklyResultsWhenNoWeeklyRulesAreConfigured() {
        List<RuleDefinition> dayRulesOnly = TestRules.defaultRules().stream()
                .filter(rule -> rule.scope() == com.atoss.timeevaluation.config.Scope.DAY)
                .toList();

        EvaluationService dailyOnly =
                new EvaluationService(new EvaluationProperties(dayRulesOnly));

        EvaluationResponse response = dailyOnly.evaluate(weekWithHours(9, 9, 8, 8, 8, 4, 0));

        assertTrue(response.weeklyResults().isEmpty());
        assertEquals(7, response.dailyResults().size());
    }

    @Test
    void shouldAllowAnyPeriodWhenNoWeeklyRulesAreConfigured() {
        List<RuleDefinition> dayRulesOnly = TestRules.defaultRules().stream()
                .filter(rule -> rule.scope() == com.atoss.timeevaluation.config.Scope.DAY)
                .toList();

        EvaluationService dailyOnly =
                new EvaluationService(new EvaluationProperties(dayRulesOnly));

        EvaluationRequest request = new EvaluationRequest(
                MONDAY, MONDAY, List.of(), fullWeek().subList(0, 1));

        assertEquals(1, dailyOnly.evaluate(request).dailyResults().size());
    }

    // ---- request validation -------------------------------------------------

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        EvaluationRequest request = new EvaluationRequest(
                MONDAY, LocalDate.of(2026, 10, 18), List.of(), List.of());

        assertThrows(IllegalArgumentException.class, () -> service.evaluateDaily(request));
    }

    @Test
    void shouldRejectIncompleteWeekForWeeklyEvaluation() {
        EvaluationRequest request = new EvaluationRequest(
                MONDAY, MONDAY.plusDays(2), List.of(), fullWeek().subList(0, 3));

        assertThrows(IllegalArgumentException.class, () -> service.evaluate(request));
    }

    @Test
    void shouldRejectOverlappingIntervals() {
        EvaluationRequest request = week(work(0, 8, 12), work(0, 11, 15));

        assertThrows(IllegalArgumentException.class, () -> service.evaluate(request));
    }

    @Test
    void shouldRejectMissingDailyInformation() {
        EvaluationRequest request = new EvaluationRequest(
                MONDAY, MONDAY.plusDays(6), List.of(), fullWeek().subList(0, 6));

        assertThrows(IllegalArgumentException.class, () -> service.evaluate(request));
    }
}
