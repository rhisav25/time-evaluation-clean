package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.service.OvertimeCalculator;

import java.util.List;

class SubtractFloorZeroRule extends BaseRule {

    private final OvertimeCalculator calculator = new OvertimeCalculator();

    SubtractFloorZeroRule(RuleDefinition definition) {
        super(definition);
    }

    @Override
    public List<String> dependencies() {
        return List.of(definition.input(), definition.subtract());
    }

    @Override
    public long evaluate(EvaluationContext context) {
        return calculator.calculateRemainingWeeklyOvertimeMinutes(
                context.valueOf(definition.input()),
                context.valueOf(definition.subtract()));
    }
}
