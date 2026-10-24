package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;

import java.util.List;

class SumOverDaysRule extends BaseRule {

    SumOverDaysRule(RuleDefinition definition) {
        super(definition);
    }

    @Override
    public List<String> dayDependencies() {
        return List.of(definition.input());
    }

    @Override
    public long evaluate(EvaluationContext context) {
        return context.dailyValues().stream()
                .mapToLong(dayValues -> dayValues.getOrDefault(definition.input(), 0L))
                .sum();
    }
}
