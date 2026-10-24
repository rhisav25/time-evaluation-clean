package com.atoss.timeevaluation.service;

import com.atoss.timeevaluation.model.AttendanceInterval;
import com.atoss.timeevaluation.model.ExpectedWorkingWindow;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttendanceCalculatorTest {

    private final AttendanceCalculator calculator =
            new AttendanceCalculator();

    @Test
    void shouldSumAttendanceIntervalsWithoutCountingGaps() {
        LocalDate date = LocalDate.of(2026, 10, 19);

        List<AttendanceInterval> intervals = List.of(
                new AttendanceInterval(
                        date,
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0)
                ),
                new AttendanceInterval(
                        date,
                        LocalTime.of(13, 0),
                        LocalTime.of(17, 0)
                )
        );

        long result = calculator.calculateAttendanceMinutes(intervals);

        assertEquals(420, result);
    }

    @Test
    void shouldCalculateTimeOutsideExpectedWindow() {
        LocalDate date = LocalDate.of(2026, 10, 19);

        List<AttendanceInterval> intervals = List.of(
                new AttendanceInterval(
                        date,
                        LocalTime.of(8, 0),
                        LocalTime.of(12, 0)
                ),
                new AttendanceInterval(
                        date,
                        LocalTime.of(13, 0),
                        LocalTime.of(18, 0)
                )
        );

        ExpectedWorkingWindow window = new ExpectedWorkingWindow(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        long result = calculator.calculateOutsideWindowMinutes(
                intervals,
                window
        );

        assertEquals(120, result);
    }

    @Test
    void shouldCalculateDailyOvertime() {
        assertEquals(
                120,
                calculator.calculateDailyOvertimeMinutes(600, 480)
        );
    }

    @Test
    void shouldReturnZeroWhenAttendanceIsBelowExpectedDuration() {
        assertEquals(
                0,
                calculator.calculateDailyOvertimeMinutes(420, 480)
        );
    }

    @Test
    void shouldCalculateSundayWork() {
        assertEquals(
                120,
                calculator.calculateSundayWorkMinutes(120, true, false)
        );
    }

    @Test
    void shouldNotCalculateSundayWageOnPublicHoliday() {
        assertEquals(
                0,
                calculator.calculateSundayWorkMinutes(120, true, true)
        );
    }

    @Test
    void shouldCalculatePublicHolidayWork() {
        assertEquals(
                180,
                calculator.calculatePublicHolidayWorkMinutes(180, true)
        );
    }


    @Test
    void shouldCorrectlyCalculateAttendanceEntirelyBeforeExpectedWindow() {
        LocalDate date = LocalDate.of(2026, 10, 19);

        List<AttendanceInterval> intervals = List.of(
                new AttendanceInterval(
                        date,
                        LocalTime.of(7, 0),
                        LocalTime.of(8, 0)
                )
        );

        ExpectedWorkingWindow window = new ExpectedWorkingWindow(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        assertEquals(
                60,
                calculator.calculateOutsideWindowMinutes(intervals, window)
        );
    }


    @Test
    void shouldNotProduceSundayWageOnPublicHoliday() {
        long attendanceMinutes = 240;

        long sundayMinutes =
                calculator.calculateSundayWorkMinutes(
                        attendanceMinutes,
                        true,
                        true
                );

        long publicHolidayMinutes =
                calculator.calculatePublicHolidayWorkMinutes(
                        attendanceMinutes,
                        true
                );

        assertEquals(0, sundayMinutes);
        assertEquals(240, publicHolidayMinutes);
    }

    @Test
    void shouldProduceSundayWageOnOrdinarySunday() {
        long sundayMinutes =
                calculator.calculateSundayWorkMinutes(
                        240,
                        true,
                        false
                );

        assertEquals(240, sundayMinutes);
    }


}