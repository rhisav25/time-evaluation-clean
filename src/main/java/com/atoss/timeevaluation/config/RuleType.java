package com.atoss.timeevaluation.config;

/** Building blocks that can be combined in application.yml. */
public enum RuleType {
    /** DAY: sum of the day's attendance intervals (gaps excluded). */
    ATTENDANCE_SUM,
    /** DAY: max(0, input - expected duration of the day), optionally only on certain weekdays. */
    EXCESS_OVER_EXPECTED,
    /** DAY: attendance before/after the expected working window. */
    OUTSIDE_WINDOW,
    /** DAY: passes the input value through only if weekday / public-holiday conditions match, else 0. */
    DAY_FILTER,
    /** WEEK: sums a DAY rule over all days of the week. */
    SUM_OVER_DAYS,
    /** any scope: max(0, input - thresholdMinutes). */
    EXCESS_OVER_THRESHOLD,
    /** any scope: max(0, input - subtract). Used to reconcile weekly and daily overtime. */
    SUBTRACT_FLOOR_ZERO
}
