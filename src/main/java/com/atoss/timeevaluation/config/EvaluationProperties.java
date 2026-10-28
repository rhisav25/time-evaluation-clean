package com.atoss.timeevaluation.config;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Rule configuration loaded from application.yml.
 */
@ConfigurationProperties(prefix = "evaluation")
public record EvaluationProperties(List<RuleDefinition> rules) {

    /**
     * One configured rule. Which fields are needed depends on the type
     * (see RuleFactory); unused fields stay null.
     *
     * @param name             name of the produced wage / value (must be unique)
     * @param type             building block to use
     * @param scope            DAY or WEEK
     * @param input            name of the rule whose value is used as input
     * @param subtract         name of the rule whose value is subtracted (SUBTRACT_FLOOR_ZERO)
     * @param thresholdMinutes fixed threshold in minutes (EXCESS_OVER_THRESHOLD)
     * @param days             weekdays the rule applies to; empty/null = every day
     * @param publicHoliday    DAY_FILTER only: required public-holiday flag; null = don't care
     * @param visible          whether the value is part of the response (default true)
     */
    public record RuleDefinition(
            String name,
            RuleType type,
            Scope scope,
            String input,
            String subtract,
            Long thresholdMinutes,
            List<DayOfWeek> days,
            Boolean publicHoliday,
            @DefaultValue("true") boolean visible
    ) {}
}
