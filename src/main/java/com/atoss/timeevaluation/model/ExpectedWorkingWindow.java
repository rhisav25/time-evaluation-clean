package com.atoss.timeevaluation.model;

import java.time.LocalTime;

public record ExpectedWorkingWindow(
        LocalTime startTime,
        LocalTime endTime
) {
}