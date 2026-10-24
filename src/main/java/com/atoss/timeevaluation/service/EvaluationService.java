package com.atoss.timeevaluation.service;

import com.atoss.timeevaluation.config.EvaluationProperties;
import com.atoss.timeevaluation.config.Scope;
import com.atoss.timeevaluation.model.*;
import com.atoss.timeevaluation.rule.EvaluationContext;
import com.atoss.timeevaluation.rule.Rule;
import com.atoss.timeevaluation.rule.RuleFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates a request and evaluates the configured rules.
 * The service contains no wage names or rule-specific logic: which wages exist,
 * how they are calculated and how they depend on each other comes from the
 * rule list in application.yml.
 */
@Service
public class EvaluationService {

    private final List<Rule> dayRules;
    private final List<Rule> weekRules;

    public EvaluationService(EvaluationProperties properties) {
        List<Rule> rules = RuleFactory.createAll(properties.rules());
        this.dayRules = rules.stream().filter(rule -> rule.scope() == Scope.DAY).toList();
        this.weekRules = rules.stream().filter(rule -> rule.scope() == Scope.WEEK).toList();
    }

    public void validateRequest(EvaluationRequest request) {
        if (request.periodEnd().isBefore(request.periodStart())) {
            throw new IllegalArgumentException(
                    "Period end must not be before period start."
            );
        }

        List<LocalDate> dates = request.dailyInformation().stream()
                .map(DailyInformation::date)
                .toList();

        long expectedDays = request.periodStart()
                .datesUntil(request.periodEnd().plusDays(1))
                .count();

        if (dates.size() != expectedDays
                || dates.stream().distinct().count() != expectedDays
                || dates.stream().anyMatch(date ->
                date.isBefore(request.periodStart())
                        || date.isAfter(request.periodEnd()))) {
            throw new IllegalArgumentException(
                    "Daily information must contain exactly one entry "
                            + "for every date in the evaluation period."
            );
        }

        // Validate expected working windows
        for (DailyInformation day : request.dailyInformation()) {
            if ((day.windowStart() == null) != (day.windowEnd() == null)) {
                throw new IllegalArgumentException(
                        "Both working window start and end must be provided together."
                );
            }

            if (day.windowStart() != null
                    && !day.windowEnd().isAfter(day.windowStart())) {
                throw new IllegalArgumentException(
                        "Working window end must be after its start."
                );
            }
        }

        // Validate attendance intervals

        for (AttendanceInterval interval : request.attendanceIntervals()) {
            if (interval.endTime().compareTo(interval.startTime()) <= 0) {
                throw new IllegalArgumentException(
                        "Attendance end time must be after start time."
                );
            }

            if (interval.date().isBefore(request.periodStart())
                    || interval.date().isAfter(request.periodEnd())) {
                throw new IllegalArgumentException(
                        "Attendance intervals must fall within the evaluation period."
                );
            }
        }

        List<AttendanceInterval> sortedIntervals =
                request.attendanceIntervals().stream()
                        .sorted(
                                java.util.Comparator
                                        .comparing(AttendanceInterval::date)
                                        .thenComparing(AttendanceInterval::startTime)
                        )
                        .toList();

        for (int i = 1; i < sortedIntervals.size(); i++) {
            AttendanceInterval previous = sortedIntervals.get(i - 1);
            AttendanceInterval current = sortedIntervals.get(i);

            if (previous.date().equals(current.date())
                    && current.startTime().isBefore(previous.endTime())) {
                throw new IllegalArgumentException(
                        "Attendance intervals must not overlap."
                );
            }
        }
    }

    public List<DailyEvaluationResult> evaluateDaily(EvaluationRequest request) {
        validateRequest(request);

        return toDailyResults(calculateDailyValues(request));
    }

    public EvaluationResponse evaluate(EvaluationRequest request) {
        // A complete Monday-Sunday week is only needed when weekly rules are configured.
        if (!weekRules.isEmpty()
                && (request.periodStart().getDayOfWeek() != DayOfWeek.MONDAY
                || request.periodEnd().getDayOfWeek() != DayOfWeek.SUNDAY
                || !request.periodEnd().equals(request.periodStart().plusDays(6)))) {
            throw new IllegalArgumentException(
                    "Weekly evaluation requires a complete Monday-to-Sunday week."
            );
        }

        validateRequest(request);

        List<DayValues> dailyValues = calculateDailyValues(request);

        List<WeeklyEvaluationResult> weeklyResults = new ArrayList<>();

        if (!weekRules.isEmpty()) {
            EvaluationContext weekContext = EvaluationContext.forWeek(
                    dailyValues.stream().map(DayValues::values).toList());

            for (Rule rule : weekRules) {
                weekContext.put(rule.name(), rule.evaluate(weekContext));
            }

            weeklyResults.add(new WeeklyEvaluationResult(
                    request.periodStart(),
                    request.periodEnd(),
                    visibleWages(weekRules, weekContext.values())
            ));
        }

        return new EvaluationResponse(toDailyResults(dailyValues), weeklyResults);
    }

    /** Runs all DAY rules, in configured order, for every day of the request. */
    private List<DayValues> calculateDailyValues(EvaluationRequest request) {
        List<DayValues> results = new ArrayList<>();

        for (DailyInformation day : request.dailyInformation()) {
            List<AttendanceInterval> intervals =
                    request.attendanceIntervals().stream()
                            .filter(interval -> interval.date().equals(day.date()))
                            .toList();

            EvaluationContext context = EvaluationContext.forDay(day, intervals);

            for (Rule rule : dayRules) {
                context.put(rule.name(), rule.evaluate(context));
            }

            results.add(new DayValues(day.date(), context.values()));
        }

        return results;
    }

    private List<DailyEvaluationResult> toDailyResults(List<DayValues> dailyValues) {
        return dailyValues.stream()
                .map(day -> new DailyEvaluationResult(
                        day.date(),
                        visibleWages(dayRules, day.values())))
                .toList();
    }

    /** Hidden rules (visible: false) are intermediate values and not part of the output. */
    private static List<Wage> visibleWages(List<Rule> rules, Map<String, Long> values) {
        return rules.stream()
                .filter(Rule::visible)
                .map(rule -> new Wage(rule.name(), values.get(rule.name())))
                .toList();
    }

    private record DayValues(LocalDate date, Map<String, Long> values) {
    }

}
