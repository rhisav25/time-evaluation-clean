
package com.atoss.timeevaluation.service;

public class OvertimeCalculator {

    public long calculateWeeklyOvertimeMinutes(
            long totalAttendanceMinutes,
            long weeklyExpectedMinutes) {

        return Math.max(
                0,
                totalAttendanceMinutes - weeklyExpectedMinutes
        );
    }

    public long calculateRemainingWeeklyOvertimeMinutes(
            long weeklyOvertimeMinutes,
            long dailyOvertimeMinutes) {

        return Math.max(
                0,
                weeklyOvertimeMinutes - dailyOvertimeMinutes
        );
    }
}
