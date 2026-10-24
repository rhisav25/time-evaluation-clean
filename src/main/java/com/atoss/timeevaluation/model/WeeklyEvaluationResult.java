
package com.atoss.timeevaluation.model;

import java.time.LocalDate;
import java.util.List;

public record WeeklyEvaluationResult(
        LocalDate periodStart,
        LocalDate periodEnd,
        List<Wage> wages
) {
}
