package com.maveric.hr360.controller;

import com.maveric.hr360.service.ReportDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/reportDetails")
public class ReportDetailController {
    private final ReportDetailsService reportDetailsService;
    @Operation(
            summary = "Fetch uniqueSurveyReportEntry"
    )
    @GetMapping(path = "/uniqueSurveyEntryReport")
    public List<String> retrieveUniqueSurveyEntry() throws IOException {
        return reportDetailsService.retrieveUniqueSurveyEntry();
    }

    @Operation(
            summary = "fetch uniqueSurveyEntryReportWithId by surveyName "
    )
    @GetMapping(path = "/uniqueSurveyEntryReportWithId/findBySurveyName")
    public Map<String, List<Long>> retrieveUniqueSurveyEntryWithEmployeeId(String surveyName) throws IOException {
        return reportDetailsService.retrieveUniqueSurveyEntryEmployeeId(surveyName);
    }

    @Operation(
            summary = "fetch all uniqueSurveyEntryReportWithId"
    )
    @GetMapping(path = "/uniqueSurveyEntryReportWithId/findAll")
    public List<Map<String, List<Long>>> retrieveAllUniqueSurveyEntryWithEmployeeId() throws IOException {
        return reportDetailsService.retrieveListOfSurveyEntryWithEmployeeId();
    }


}
