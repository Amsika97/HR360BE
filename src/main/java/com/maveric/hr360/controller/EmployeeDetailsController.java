package com.maveric.hr360.controller;

import com.maveric.hr360.service.implementation.EmployeeDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/employeeDetails")
@RequiredArgsConstructor
public class EmployeeDetailsController {


    private final EmployeeDetailsServiceImpl employeeDetailsService;

    @Operation(
            summary = "Save EmployeeDetails"
    )
    @PostMapping(path = "/saveEmployeeDetails", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String, List<String>> saveExcelDataForEmployeeDetails(@RequestParam MultipartFile multipartFile) throws IOException {
        File file = employeeDetailsService.convertMultiPartToPath(multipartFile);
        return employeeDetailsService.getExcelDataAsList(file);

    }
}
