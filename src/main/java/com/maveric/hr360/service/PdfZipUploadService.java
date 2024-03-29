package com.maveric.hr360.service;

import jakarta.servlet.http.HttpServletResponse;;
import java.io.IOException;
import java.util.List;

public interface PdfZipUploadService {
    public String createZipForPdfUpload(String zipFileName, List<String> filePaths);
    public void getZipFile(HttpServletResponse response, String zipFileName) throws IOException;
}
