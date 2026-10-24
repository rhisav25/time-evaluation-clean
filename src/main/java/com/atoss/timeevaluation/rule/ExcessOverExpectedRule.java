package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.service.AttendanceCalculator;

import java.util.List;

class ExcessOverExpectedRule extends BaseRule {

    private final AttendanceCalculator calculator = new AttendanceCalculator();

    ExcessOverExpectedRule(RuleDefinition definition) {
        super(definition);
    }

    @Override
    public List<String> dependencies() {
        return List.of(definition.input());
    }

    @Override
    public long evaluate(EvaluationContext context) {
        if (!RuleSupport.appliesOn(definition.days(), context.day().date().getDayOfWeek())) {
            return 0;
        }
        return calculator.calculateDailyOvertimeMinutes(
                context.valueOf(definition.input()),
                context.day().expectedDurationMinutes());
    }
}
