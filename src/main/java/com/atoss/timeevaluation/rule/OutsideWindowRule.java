package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.model.DailyInformation;
import com.atoss.timeevaluation.model.ExpectedWorkingWindow;
import com.atoss.timeevaluation.service.AttendanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OutsideWindowRule extends BaseRule {

    private static final Logger log = LoggerFactory.getLogger(OutsideWindowRule.class);

    private final AttendanceCalculator calculator = new AttendanceCalculator();

    OutsideWindowRule(RuleDefinition definition) {
        super(definition);
    }

    @Override
    public long evaluate(EvaluationContext context) {
        DailyInformation day = context.day();
        if (day.windowStart() == null || day.windowEnd() == null) {
            log.debug("Rule '{}': {} has no working window, result 0", name(), day.date());
            return 0;
        }
        long outside = calculator.calculateOutsideWindowMinutes(
                context.intervals(),
                new ExpectedWorkingWindow(day.windowStart(), day.windowEnd()));
        log.debug("Rule '{}': {} minute(s) outside window {}-{} on {}",
                name(), outside, day.windowStart(), day.windowEnd(), day.date());
        return outside;
    }
}
