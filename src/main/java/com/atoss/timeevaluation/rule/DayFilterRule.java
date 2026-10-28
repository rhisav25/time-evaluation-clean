package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Passes the input through when the day matches, otherwise 0.
 * Sunday work = days [SUNDAY] + publicHoliday false;
 * public-holiday work = publicHoliday true. That is how "holiday wins" is configured.
 */
class DayFilterRule extends BaseRule {

    private static final Logger log = LoggerFactory.getLogger(DayFilterRule.class);

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

        if (weekdayMatches && holidayMatches) {
            long value = context.valueOf(definition.input());
            log.debug("Rule '{}': {} matches filter, passing through {} minute(s)",
                    name(), context.day().date(), value);
            return value;
        }

        log.debug("Rule '{}': {} filtered out (weekdayMatches={}, holidayMatches={}), result 0",
                name(), context.day().date(), weekdayMatches, holidayMatches);
        return 0;
    }
}
