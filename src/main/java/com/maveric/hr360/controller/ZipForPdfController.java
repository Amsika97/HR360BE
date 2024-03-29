package com.maveric.hr360.controller;

import com.maveric.hr360.service.PdfZipUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ZipForPdfController {
    private final PdfZipUploadService pdfZipUploadService;
    @PostMapping("/create-zip/{zipFileName}")
    public String createZipForPdfUpload(@PathVariable String zipFileName,
                                        @RequestBody List<String> filePaths) throws IOException {
        return pdfZipUploadService.createZipForPdfUpload(zipFileName, filePaths);
    }
    @GetMapping("/getZipFile/{zipFileName}")
    public void getZipFileName(HttpServletResponse response, @PathVariable String zipFileName) throws IOException {
         pdfZipUploadService.getZipFile(response,zipFileName);
    }
}
