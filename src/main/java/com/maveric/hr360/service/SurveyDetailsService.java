package com.maveric.hr360.service;


import com.maveric.hr360.entity.SurveyDetails;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public interface SurveyDetailsService {
    Map<String, ArrayList> getExcelDataAsList(List<File> fileList) throws IOException;

    Map<String, ArrayList<String>> saveExcelData(Map<String, ArrayList> excelDataAsMap, SurveyDetails invoices);

    SurveyDetails setSurveyDetails(Map<String, ArrayList> excelDataAsMap);

    List<File> convertMultiPartToPath(MultipartFile[] multiFiles)
            throws IllegalStateException, IOException;

}
