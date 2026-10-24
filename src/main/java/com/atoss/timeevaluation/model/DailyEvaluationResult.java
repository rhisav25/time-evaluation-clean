
package com.atoss.timeevaluation.model;

import java.time.LocalDate;
import java.util.List;

public record DailyEvaluationResult(
        LocalDate date,
        List<Wage> wages
) {
}
