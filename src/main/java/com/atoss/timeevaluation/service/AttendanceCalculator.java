package com.atoss.timeevaluation.service;

import com.atoss.timeevaluation.model.AttendanceInterval;
import com.atoss.timeevaluation.model.ExpectedWorkingWindow;

import java.time.Duration;
import java.util.List;

public class AttendanceCalculator {

    public long calculateAttendanceMinutes(
            List<AttendanceInterval> intervals) {

        return intervals.stream()
                .mapToLong(interval ->
                        Duration.between(
                                interval.startTime(),
                                interval.endTime()
                        ).toMinutes()
                )
                .sum();
    }


    public long calculateOutsideWindowMinutes(
            List<AttendanceInterval> intervals,
            ExpectedWorkingWindow window) {

        return intervals.stream()
                .mapToLong(interval -> {
                    long beforeWindow = 0;

                    if (interval.startTime().isBefore(window.startTime())) {
                        beforeWindow = Duration.between(
                                interval.startTime(),
                                interval.endTime().isBefore(window.startTime())
                                        ? interval.endTime()
                                        : window.startTime()
                        ).toMinutes();
                    }

                    long afterWindow = 0;

                    if (interval.endTime().isAfter(window.endTime())) {
                        afterWindow = Duration.between(
                                interval.startTime().isAfter(window.endTime())
                                        ? interval.startTime()
                                        : window.endTime(),
                                interval.endTime()
                        ).toMinutes();
                    }

                    return beforeWindow + afterWindow;
                })
                .sum();
    }

    public long calculateDailyOvertimeMinutes(
            long attendanceMinutes,
            long expectedDurationMinutes) {

        return Math.max(
                0,
                attendanceMinutes - expectedDurationMinutes
        );
    }

    public long calculateSundayWorkMinutes(
            long attendanceMinutes,
            boolean isSunday,
            boolean isPublicHoliday) {

        if (!isSunday || isPublicHoliday) {
            return 0;
        }

        return attendanceMinutes;
    }

    public long calculatePublicHolidayWorkMinutes(
            long attendanceMinutes,
            boolean isPublicHoliday) {

        if (!isPublicHoliday) {
            return 0;
        }

        return attendanceMinutes;
    }
}