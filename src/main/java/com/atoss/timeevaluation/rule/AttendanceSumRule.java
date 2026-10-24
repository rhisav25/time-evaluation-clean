package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.service.AttendanceCalculator;

class AttendanceSumRule extends BaseRule {

    private final AttendanceCalculator calculator = new AttendanceCalculator();

    AttendanceSumRule(RuleDefinition definition) {
        super(definition);
    }

    @Override
    public long evaluate(EvaluationContext context) {
        return calculator.calculateAttendanceMinutes(context.intervals());
    }
}
