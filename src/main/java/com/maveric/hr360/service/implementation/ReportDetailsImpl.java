package com.maveric.hr360.service.implementation;

import com.maveric.hr360.entity.ReportDetails;
import com.maveric.hr360.repository.ReportDetailsRepository;

import com.maveric.hr360.service.ReportDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ReportDetailsImpl implements ReportDetailsService {
    private final ReportDetailsRepository reportDetailsRepository;

    @Override
    public List<String> retrieveUniqueSurveyEntry() throws IOException {
        log.info("ReportDetailsImpl::retrieveUniqueSurveyEntry()::start");
        List<String> uniqueSurveyList = reportDetailsRepository.findAll().stream().map(ReportDetails::getSurveyName).distinct().collect(Collectors.toList());
        log.info("ReportDetailsImpl::retrieveUniqueSurveyEntry()::end");
        return uniqueSurveyList;
    }

    public Map<String, List<Long>> retrieveUniqueSurveyEntryEmployeeId(String surveyName) throws IOException {
        log.info("ReportDetailsImpl::retrieveUniqueSurveyEntryEmployeeId()::start");
        try {
            List<ReportDetails> reportList = reportDetailsRepository.findBySurveyName(surveyName);//find by surveyName
            Map<String, List<Long>> responseMap = new HashMap<>();

            List<Long> empIdList = new ArrayList<>();

            for (ReportDetails reportDetail : reportList) {
                Long employeeId = reportDetail.getEmployeeId();
                empIdList.add(employeeId);
            }
            responseMap.put(surveyName, empIdList);
            log.info("ReportDetailsImpl::retrieveUniqueSurveyEntryEmployeeId()::end");
            return responseMap;
        } catch (RuntimeException e) {
            log.error(e.getMessage() + "No data found for surveyName:" + surveyName);
            return null;
        }


    }

    public List<Map<String, List<Long>>> retrieveListOfSurveyEntryWithEmployeeId() throws IOException {
        log.info("ReportDetailsImpl::retrieveListOfSurveyEntryWithEmployeeId()::start");
        List<String> surveyNameList = retrieveUniqueSurveyEntry();
        List<ReportDetails> reportList = reportDetailsRepository.findAll();
        List<Long> employeeIDList;
        List<Map<String, List<Long>>> finalResponse = new ArrayList<>();
        Map<String, List<Long>> responseMap = new HashMap<>();
        try {
            for (String surveyName : surveyNameList) {
                employeeIDList = new ArrayList<>();
                for (ReportDetails reportDetail : reportList) {
                    if (reportDetail.getSurveyName().equalsIgnoreCase(surveyName))
                        employeeIDList.add(reportDetail.getEmployeeId());
                }
                if (!employeeIDList.isEmpty())
                    responseMap.put(surveyName, employeeIDList);
            }
            finalResponse.add(responseMap);
            log.info("ReportDetailsImpl::retrieveListOfSurveyEntryWithEmployeeId()::end");
            return finalResponse;
        } catch (Exception e) {
            log.error(e.getMessage() + "Exception occurred during data fetch operation");
            return null;
        }

    }
}
