package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.model.DailyInformation;
import com.atoss.timeevaluation.model.ExpectedWorkingWindow;
import com.atoss.timeevaluation.service.AttendanceCalculator;

class OutsideWindowRule extends BaseRule {

    private final AttendanceCalculator calculator = new AttendanceCalculator();

    OutsideWindowRule(RuleDefinition definition) {
        super(definition);
    }

    @Override
    public long evaluate(EvaluationContext context) {
        DailyInformation day = context.day();
        if (day.windowStart() == null || day.windowEnd() == null) {
            return 0;
        }
        return calculator.calculateOutsideWindowMinutes(
                context.intervals(),
                new ExpectedWorkingWindow(day.windowStart(), day.windowEnd()));
    }
}
