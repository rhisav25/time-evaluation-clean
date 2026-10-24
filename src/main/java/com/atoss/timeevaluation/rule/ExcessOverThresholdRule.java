package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.service.OvertimeCalculator;

import java.util.List;

class ExcessOverThresholdRule extends BaseRule {

    private final OvertimeCalculator calculator = new OvertimeCalculator();

    ExcessOverThresholdRule(RuleDefinition definition) {
        super(definition);
    }

    @Override
    public List<String> dependencies() {
        return List.of(definition.input());
    }

    @Override
    public long evaluate(EvaluationContext context) {
        return calculator.calculateWeeklyOvertimeMinutes(
                context.valueOf(definition.input()),
                definition.thresholdMinutes());
    }
}
