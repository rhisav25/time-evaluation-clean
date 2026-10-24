package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.config.Scope;

import java.util.List;

public interface Rule {

    String name();

    Scope scope();

    /** Whether the value is returned in the response (hidden rules are only intermediate values). */
    boolean visible();

    /** Minutes produced by this rule. */
    long evaluate(EvaluationContext context);

    /** Earlier rules of the same scope this rule reads. */
    default List<String> dependencies() {
        return List.of();
    }

    /** Earlier DAY-scope rules this (WEEK) rule reads across the week. */
    default List<String> dayDependencies() {
        return List.of();
    }
}
