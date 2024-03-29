package com.maveric.hr360.controller;

import com.maveric.hr360.service.ScoreCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScoreCalculationController {
    private final ScoreCalculationService scoreCalculationService;
    @PostMapping(path = "/scorecalculation/{surveyName}")
    public Map<String, ArrayList<String>> scoreCalculation(@PathVariable String surveyName ) {
        log.info("Scorecalculationcontroller::scoreCalulation method ::started");
        return scoreCalculationService.scoreCalculation(surveyName);

    }
}
