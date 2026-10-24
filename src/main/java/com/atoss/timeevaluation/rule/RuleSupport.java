package com.atoss.timeevaluation.rule;

import java.time.DayOfWeek;
import java.util.List;

final class RuleSupport {

    private RuleSupport() {
    }

    /** An empty or missing weekday list means "every day". */
    static boolean appliesOn(List<DayOfWeek> days, DayOfWeek day) {
        return days == null || days.isEmpty() || days.contains(day);
    }
}
