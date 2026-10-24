
package com.atoss.timeevaluation.model;

import java.util.List;

public record EvaluationResponse(
        List<DailyEvaluationResult> dailyResults,
        List<WeeklyEvaluationResult> weeklyResults
) {
}
