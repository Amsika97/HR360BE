package com.maveric.hr360.service;


import com.maveric.hr360.entity.AbsoluteScore;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface AbsoluteScoreDetailsService {
    Map<String, String> getExcelDataAsList(File file);

    public File convertMultiPartToPath(MultipartFile multiFile)
            throws IllegalStateException, IOException;

    Map<String, String> saveExcelData(AbsoluteScore absoluteScore, File file, List<AbsoluteScore> absoluteScoreList);

    void downloadFile(HttpServletResponse response, String tableName) throws IOException;

}
