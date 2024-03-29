package com.maveric.hr360.service;

import java.util.ArrayList;
import java.util.Map;

public interface ScoreCalculationService {
    Map<String, ArrayList<String>> scoreCalculation(String surveyName);
}
