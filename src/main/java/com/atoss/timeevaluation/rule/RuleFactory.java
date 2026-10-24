package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.config.RuleType;
import com.atoss.timeevaluation.config.Scope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns configured rule definitions into rule objects and validates the
 * configuration once at startup, so mistakes fail fast with a clear message
 * instead of surfacing as wrong numbers later.
 *
 * Rules are evaluated in the configured order, so a rule may only reference
 * rules defined above it. This also rules out circular references.
 */
public final class RuleFactory {

    private RuleFactory() {
    }

    public static List<Rule> createAll(List<RuleDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            throw new IllegalStateException("No rules configured under 'evaluation.rules'.");
        }

        Map<String, Scope> defined = new HashMap<>();
        List<Rule> rules = new java.util.ArrayList<>();

        for (RuleDefinition definition : definitions) {
            Rule rule = create(definition);

            if (defined.containsKey(rule.name())) {
                throw invalid(rule.name(), "the name is used more than once");
            }

            for (String dependency : rule.dependencies()) {
                if (defined.get(dependency) != rule.scope()) {
                    throw invalid(rule.name(), "'" + dependency + "' must be a "
                            + rule.scope() + " rule defined above it");
                }
            }
            for (String dependency : rule.dayDependencies()) {
                if (defined.get(dependency) != Scope.DAY) {
                    throw invalid(rule.name(), "'" + dependency
                            + "' must be a DAY rule defined above it");
                }
            }

            defined.put(rule.name(), rule.scope());
            rules.add(rule);
        }

        return List.copyOf(rules);
    }

    private static Rule create(RuleDefinition d) {
        if (d == null || d.name() == null || d.name().isBlank()) {
            throw new IllegalStateException("Every rule needs a 'name'.");
        }
        if (d.type() == null) {
            throw invalid(d.name(), "'type' is required");
        }
        if (d.scope() == null) {
            throw invalid(d.name(), "'scope' is required");
        }

        RuleType type = d.type();
        switch (type) {
            case ATTENDANCE_SUM, OUTSIDE_WINDOW -> requireScope(d, Scope.DAY);
            case EXCESS_OVER_EXPECTED, DAY_FILTER -> {
                requireScope(d, Scope.DAY);
                requireText(d, "input", d.input());
            }
            case SUM_OVER_DAYS -> {
                requireScope(d, Scope.WEEK);
                requireText(d, "input", d.input());
            }
            case EXCESS_OVER_THRESHOLD -> {
                requireText(d, "input", d.input());
                if (d.thresholdMinutes() == null || d.thresholdMinutes() < 0) {
                    throw invalid(d.name(), "'thresholdMinutes' must be 0 or more");
                }
            }
            case SUBTRACT_FLOOR_ZERO -> {
                requireText(d, "input", d.input());
                requireText(d, "subtract", d.subtract());
            }
        }

        return switch (type) {
            case ATTENDANCE_SUM -> new AttendanceSumRule(d);
            case EXCESS_OVER_EXPECTED -> new ExcessOverExpectedRule(d);
            case OUTSIDE_WINDOW -> new OutsideWindowRule(d);
            case DAY_FILTER -> new DayFilterRule(d);
            case SUM_OVER_DAYS -> new SumOverDaysRule(d);
            case EXCESS_OVER_THRESHOLD -> new ExcessOverThresholdRule(d);
            case SUBTRACT_FLOOR_ZERO -> new SubtractFloorZeroRule(d);
        };
    }

    private static void requireScope(RuleDefinition d, Scope expected) {
        if (d.scope() != expected) {
            throw invalid(d.name(), d.type() + " must have scope " + expected);
        }
    }

    private static void requireText(RuleDefinition d, String field, String value) {
        if (value == null || value.isBlank()) {
            throw invalid(d.name(), "'" + field + "' is required for " + d.type());
        }
    }

    private static IllegalStateException invalid(String ruleName, String reason) {
        return new IllegalStateException("Invalid rule '" + ruleName + "': " + reason + ".");
    }
}
