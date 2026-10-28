package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.service.AttendanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class ExcessOverExpectedRule extends BaseRule {

    private static final Logger log = LoggerFactory.getLogger(ExcessOverExpectedRule.class);

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
            log.debug("Rule '{}': {} not among configured days, result 0",
                    name(), context.day().date().getDayOfWeek());
            return 0;
        }
        long overtime = calculator.calculateDailyOvertimeMinutes(
                context.valueOf(definition.input()),
                context.day().expectedDurationMinutes());
        log.debug("Rule '{}': {} minute(s) overtime ({} worked vs {} expected)",
                name(), overtime, context.valueOf(definition.input()),
                context.day().expectedDurationMinutes());
        return overtime;
    }
}
