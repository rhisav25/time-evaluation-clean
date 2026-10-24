package com.atoss.timeevaluation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/** Loads the real application.yml, so these tests also prove the shipped rule config is valid. */
@SpringBootTest
@AutoConfigureMockMvc
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldEvaluateAWeekThroughRestApi() throws Exception {
        String request = """
                {
                  "periodStart": "2026-10-19",
                  "periodEnd": "2026-10-25",
                  "attendanceIntervals": [
                    {
                      "date": "2026-10-19",
                      "startTime": "08:00",
                      "endTime": "18:00"
                    }
                  ],
                  "dailyInformation": [
                    {"date":"2026-10-19","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":"08:00","windowEnd":"18:00"},
                    {"date":"2026-10-20","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":"08:00","windowEnd":"18:00"},
                    {"date":"2026-10-21","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":"08:00","windowEnd":"18:00"},
                    {"date":"2026-10-22","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":"08:00","windowEnd":"18:00"},
                    {"date":"2026-10-23","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":"08:00","windowEnd":"18:00"},
                    {"date":"2026-10-24","publicHoliday":false,"expectedDurationMinutes":0,"windowStart":null,"windowEnd":null},
                    {"date":"2026-10-25","publicHoliday":false,"expectedDurationMinutes":0,"windowStart":null,"windowEnd":null}
                  ]
                }
                """;

        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyResults.length()").value(7))
                .andExpect(jsonPath("$.weeklyResults.length()").value(1))
                .andExpect(jsonPath("$.dailyResults[0].date").value("2026-10-19"))
                .andExpect(jsonPath("$.dailyResults[0].wages[0].name").value("attendance"))
                .andExpect(jsonPath("$.dailyResults[0].wages[0].durationMinutes").value(600))
                .andExpect(jsonPath("$.dailyResults[0].wages[1].name").value("daily-overtime"))
                .andExpect(jsonPath("$.dailyResults[0].wages[1].durationMinutes").value(120))
                .andExpect(jsonPath("$.weeklyResults[0].wages.length()").value(1))
                .andExpect(jsonPath("$.weeklyResults[0].wages[0].name").value("weekly-overtime"));
    }

    @Test
    void shouldReturnBadRequestForIncompleteWeek() throws Exception {
        String request = """
                {
                  "periodStart": "2026-10-20",
                  "periodEnd": "2026-10-26",
                  "attendanceIntervals": [],
                  "dailyInformation": [
                    {"date":"2026-10-20","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":null,"windowEnd":null},
                    {"date":"2026-10-21","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":null,"windowEnd":null},
                    {"date":"2026-10-22","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":null,"windowEnd":null},
                    {"date":"2026-10-23","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":null,"windowEnd":null},
                    {"date":"2026-10-24","publicHoliday":false,"expectedDurationMinutes":0,"windowStart":null,"windowEnd":null},
                    {"date":"2026-10-25","publicHoliday":false,"expectedDurationMinutes":0,"windowStart":null,"windowEnd":null},
                    {"date":"2026-10-26","publicHoliday":false,"expectedDurationMinutes":480,"windowStart":null,"windowEnd":null}
                  ]
                }
                """;

        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturnBadRequestForMalformedBody() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodStart\": \"not-a-date\"}"))
                .andExpect(status().isBadRequest());
    }
}
