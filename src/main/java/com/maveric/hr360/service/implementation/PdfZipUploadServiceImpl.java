package com.maveric.hr360.service.implementation;

import com.maveric.hr360.Exception.CustomException;
import com.maveric.hr360.service.PdfZipUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfZipUploadServiceImpl implements PdfZipUploadService {
    @Value("${zipFilePath}")
    private String zipfilePath;

    public String createZipForPdfUpload(String zipFileName, List<String> filePaths) {
        try {
            String zipFilePath = zipfilePath + zipFileName + ".zip";
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (String filePath : filePaths) {
                File file = new File(filePath);
                if (!file.exists()) {
                    continue;
                }
                ZipEntry pdfEntry = new ZipEntry(file.getName());
                zos.putNextEntry(pdfEntry);
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                fis.close();
                zos.closeEntry();
            }
            zos.close();
            return zipFilePath;
        } catch (IOException e) {
            log.error(e.getMessage() + " " + zipfilePath + zipFileName);
            return null;
        }
    }

    @Override
    public void getZipFile(HttpServletResponse response, String zipFileName) throws IOException {
        log.info("PdfZipUploadServiceImpl :: getZipFile :: method started");
        String newZipFileName = zipFileName.replace("-", "_").replace(":", "_");
        String zipFilePath = zipfilePath + newZipFileName + ".zip";
        File file = new File(zipFilePath);
        if (file.exists()) {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename="+newZipFileName+".zip");
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
                log.info("File {} downloaded successfully", zipFilePath);
            } catch (IOException e) {
                log.error("Error reading file: {}", e.getMessage());
                throw new IOException("Error reading file");
            }
        } else {
            log.error("File not found: {}", zipFilePath);
            throw new CustomException("File not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("PdfZipUploadServiceImpl :: getZipFile :: method end");
    }

}