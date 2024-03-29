package com.maveric.hr360.controller;
import com.maveric.hr360.service.AbsoluteScoreDetailsService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/absoluteScoreDetails")
@RequiredArgsConstructor
public class AbsoluteScoreDetailsController {

    private final AbsoluteScoreDetailsService absoluteScoreDetailsService;
    @Operation(
            summary = "Save AbsoluteScoreDetails"
    )
    @PostMapping(path="/saveAbsoluteScoreDetails",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Map<String, String> saveExcelDataForAbsoluteScore(@RequestParam MultipartFile multipartFile) throws IOException {
            File file=absoluteScoreDetailsService.convertMultiPartToPath(multipartFile);
            return  absoluteScoreDetailsService.getExcelDataAsList(file);
        }

    @Operation(
            summary = "Fetch AbsoluteScoreDetails"
    )
    @GetMapping(path="/downloadFile")
    public void fetchDataForCollection(HttpServletResponse response,@RequestParam String tableName) throws IOException {
        absoluteScoreDetailsService.downloadFile(response,tableName);

    }
}


