package com.maveric.hr360.service;


import java.io.IOException;

import java.util.List;
import java.util.Map;

public interface ReportDetailsService {
    List<String> retrieveUniqueSurveyEntry() throws IOException;

    Map<String, List<Long>> retrieveUniqueSurveyEntryEmployeeId(String surveyName) throws IOException;

    List<Map<String, List<Long>>> retrieveListOfSurveyEntryWithEmployeeId() throws IOException;
}
