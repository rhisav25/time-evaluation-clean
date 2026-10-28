
package com.atoss.timeevaluation.controller;

import com.atoss.timeevaluation.model.EvaluationRequest;
import com.atoss.timeevaluation.model.EvaluationResponse;
import com.atoss.timeevaluation.service.EvaluationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private static final Logger log = LoggerFactory.getLogger(EvaluationController.class);

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public EvaluationResponse evaluate(
            @Valid @RequestBody EvaluationRequest request) {
        log.info("POST /api/evaluations: period {} to {}, {} interval(s), {} day(s)",
                request.periodStart(), request.periodEnd(),
                request.attendanceIntervals().size(), request.dailyInformation().size());

        EvaluationResponse response = evaluationService.evaluate(request);

        log.info("POST /api/evaluations done: {} daily result(s), {} weekly result(s)",
                response.dailyResults().size(), response.weeklyResults().size());
        return response;
    }
}
