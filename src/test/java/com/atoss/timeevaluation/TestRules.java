package com.atoss.timeevaluation;

import com.atoss.timeevaluation.config.EvaluationProperties;
import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.config.RuleType;
import com.atoss.timeevaluation.config.Scope;
import com.atoss.timeevaluation.model.Wage;

import java.time.DayOfWeek;
import java.util.List;

/** Test helper: the same rule set as application.yml, built in Java. */
public final class TestRules {

    public static final List<DayOfWeek> MONDAY_TO_FRIDAY = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private TestRules() {
    }

    public static EvaluationProperties defaultProperties() {
        return new EvaluationProperties(defaultRules());
    }

    public static List<RuleDefinition> defaultRules() {
        return defaultRules(MONDAY_TO_FRIDAY, 2400);
    }

    public static List<RuleDefinition> defaultRules(
            List<DayOfWeek> overtimeDays, long weeklyThresholdMinutes) {
        return List.of(
                rule("attendance", RuleType.ATTENDANCE_SUM, Scope.DAY),
                new RuleDefinition("daily-overtime", RuleType.EXCESS_OVER_EXPECTED, Scope.DAY,
                        "attendance", null, null, overtimeDays, null, true),
                rule("outside-window", RuleType.OUTSIDE_WINDOW, Scope.DAY),
                new RuleDefinition("sunday-work", RuleType.DAY_FILTER, Scope.DAY,
                        "attendance", null, null, List.of(DayOfWeek.SUNDAY), false, true),
                new RuleDefinition("public-holiday-work", RuleType.DAY_FILTER, Scope.DAY,
                        "attendance", null, null, null, true, true),
                new RuleDefinition("week-attendance", RuleType.SUM_OVER_DAYS, Scope.WEEK,
                        "attendance", null, null, null, null, false),
                new RuleDefinition("week-daily-overtime", RuleType.SUM_OVER_DAYS, Scope.WEEK,
                        "daily-overtime", null, null, null, null, false),
                new RuleDefinition("week-overtime-before-reconciliation",
                        RuleType.EXCESS_OVER_THRESHOLD, Scope.WEEK,
                        "week-attendance", null, weeklyThresholdMinutes, null, null, false),
                new RuleDefinition("weekly-overtime", RuleType.SUBTRACT_FLOOR_ZERO, Scope.WEEK,
                        "week-overtime-before-reconciliation", "week-daily-overtime",
                        null, null, null, true)
        );
    }

    /** Rule without any parameters. */
    public static RuleDefinition rule(String name, RuleType type, Scope scope) {
        return new RuleDefinition(name, type, scope, null, null, null, null, null, true);
    }

    public static long minutes(List<Wage> wages, String name) {
        return wages.stream()
                .filter(wage -> wage.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No wage named " + name))
                .durationMinutes();
    }
}
