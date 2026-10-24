package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.model.AttendanceInterval;
import com.atoss.timeevaluation.model.DailyInformation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data a rule can read while it is evaluated, plus the values already
 * computed by earlier rules of the same scope.
 */
public final class EvaluationContext {

    private final DailyInformation day;                    // DAY scope only
    private final List<AttendanceInterval> intervals;      // DAY scope only
    private final List<Map<String, Long>> dailyValues;     // WEEK scope only
    private final Map<String, Long> values = new LinkedHashMap<>();

    private EvaluationContext(
            DailyInformation day,
            List<AttendanceInterval> intervals,
            List<Map<String, Long>> dailyValues) {
        this.day = day;
        this.intervals = intervals;
        this.dailyValues = dailyValues;
    }

    public static EvaluationContext forDay(
            DailyInformation day, List<AttendanceInterval> intervals) {
        return new EvaluationContext(day, intervals, List.of());
    }

    public static EvaluationContext forWeek(List<Map<String, Long>> dailyValues) {
        return new EvaluationContext(null, List.of(), dailyValues);
    }

    public DailyInformation day() {
        return day;
    }

    public List<AttendanceInterval> intervals() {
        return intervals;
    }

    public List<Map<String, Long>> dailyValues() {
        return dailyValues;
    }

    public Map<String, Long> values() {
        return values;
    }

    public long valueOf(String ruleName) {
        Long value = values.get(ruleName);
        if (value == null) {
            throw new IllegalStateException(
                    "Rule '" + ruleName + "' has not been evaluated yet.");
        }
        return value;
    }

    public void put(String ruleName, long value) {
        values.put(ruleName, value);
    }
}
