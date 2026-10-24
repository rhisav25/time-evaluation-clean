package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;

import java.util.List;

/**
 * Passes the input through when the day matches, otherwise 0.
 * Sunday work = days [SUNDAY] + publicHoliday false;
 * public-holiday work = publicHoliday true. That is how "holiday wins" is configured.
 */
class DayFilterRule extends BaseRule {

    DayFilterRule(RuleDefinition definition) {
        super(definition);
    }

    @Override
    public List<String> dependencies() {
        return List.of(definition.input());
    }

    @Override
    public long evaluate(EvaluationContext context) {
        boolean weekdayMatches = RuleSupport.appliesOn(
                definition.days(), context.day().date().getDayOfWeek());
        boolean holidayMatches = definition.publicHoliday() == null
                || definition.publicHoliday() == context.day().publicHoliday();

        return weekdayMatches && holidayMatches
                ? context.valueOf(definition.input())
                : 0;
    }
}
