
package com.atoss.timeevaluation.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record DailyInformation(
        @NotNull LocalDate date,
        boolean publicHoliday,
        @Min(0) long expectedDurationMinutes,
        LocalTime windowStart,
        LocalTime windowEnd
) {
}
