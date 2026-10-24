
package com.atoss.timeevaluation.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record EvaluationRequest(
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        @NotNull @Valid List<AttendanceInterval> attendanceIntervals,
        @NotEmpty @Valid List<DailyInformation> dailyInformation
) {
}
