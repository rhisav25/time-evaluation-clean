
package com.atoss.timeevaluation.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OvertimeCalculatorTest {

    private final OvertimeCalculator calculator =
            new OvertimeCalculator();

    @Test
    void shouldCalculateWeeklyOvertime() {
        assertEquals(
                120,
                calculator.calculateWeeklyOvertimeMinutes(
                        2520, 2400
                )
        );
    }

    @Test
    void shouldSubtractDailyOvertimeFromWeeklyOvertime() {
        assertEquals(
                0,
                calculator.calculateRemainingWeeklyOvertimeMinutes(
                        120, 240
                )
        );
    }

    @Test
    void shouldKeepWeeklyOvertimeRemainingWhenItExceedsDailyOvertime() {
        assertEquals(
                240,
                calculator.calculateRemainingWeeklyOvertimeMinutes(
                        360, 120
                )
        );
    }

    @Test
    void shouldNeverReturnNegativeWeeklyOvertime() {
        assertEquals(
                0,
                calculator.calculateRemainingWeeklyOvertimeMinutes(
                        120, 180
                )
        );
    }
}
