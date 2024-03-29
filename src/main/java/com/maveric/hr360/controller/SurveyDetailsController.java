package com.maveric.hr360.controller;


import com.maveric.hr360.entity.SurveyDetails;
import com.maveric.hr360.service.SurveyDetailsService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/surveyDetails")
@RequiredArgsConstructor
public class SurveyDetailsController {

    private final SurveyDetailsService surveyDetailsService;

    @Operation(
            summary = "Save SurveyDetails"
    )
    @PostMapping(path = "/saveSurveyDetails", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String, ArrayList<String>> saveExcelDataForSurveyDetails(@RequestPart MultipartFile[] files) throws IOException {
        log.debug("SurveyDetailsController::saveExcelDataForSurveyDetails()::{}", files);
        List<File> fileList = surveyDetailsService.convertMultiPartToPath(files);
        Map<String, ArrayList> excelDataAsMap = surveyDetailsService.getExcelDataAsList(fileList);
        SurveyDetails surveyDetails = surveyDetailsService.setSurveyDetails(excelDataAsMap);
        return surveyDetailsService.saveExcelData(excelDataAsMap, surveyDetails);
    }

}
