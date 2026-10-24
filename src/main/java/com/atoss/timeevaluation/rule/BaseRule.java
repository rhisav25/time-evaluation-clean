package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.config.Scope;

abstract class BaseRule implements Rule {

    protected final RuleDefinition definition;

    BaseRule(RuleDefinition definition) {
        this.definition = definition;
    }

    @Override
    public String name() {
        return definition.name();
    }

    @Override
    public Scope scope() {
        return definition.scope();
    }

    @Override
    public boolean visible() {
        return definition.visible();
    }
}
