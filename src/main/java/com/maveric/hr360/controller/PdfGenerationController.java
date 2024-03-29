package com.maveric.hr360.controller;

import com.maveric.hr360.Exception.CustomException;
import com.maveric.hr360.entity.AbsoluteScore;
import com.maveric.hr360.repository.AbsoluteScoreRepository;
import com.maveric.hr360.service.implementation.EChartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


@RestController
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationController {

    private final EChartService eChartService ;
    private final AbsoluteScoreRepository absoluteScoreRepository;

    @PostMapping("/generatePdf")
    public String generateChart() throws Exception {
        return eChartService.generateChart("survey_2024-02-28T19:50:04","Lead");
    }

    @GetMapping("/generatePdf/{employeeId}/{surveyId}")
    public ResponseEntity<byte[]> generateIndividualPdf(@PathVariable Long employeeId, @PathVariable String surveyId) {
        try{
            AbsoluteScore absoluteScore = absoluteScoreRepository.findByEmployeeIdAndSurveyName(String.valueOf(employeeId),surveyId);
            File file = eChartService.generateChartForIndividual(employeeId,surveyId,absoluteScore.getGrade());
            byte[] fileContent = Files.readAllBytes(file.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(file.getName(), file.getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .body(fileContent);
        }catch (Exception e){
            log.error(e.getMessage());
            String errorMessage = "Exception occurred while creating pdf";
            byte[] errorBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorBytes);
        }

    }

}

