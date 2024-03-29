package com.maveric.hr360.controller;

import com.maveric.hr360.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/employee")
public class EmployeeController {
    private final EmployeeService employeeService;
    @PostMapping(path = "/saveImageFromZip")
    public ResponseEntity<Map<String, ArrayList<String>>>uploadImageZip(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity
                .ok(employeeService.saveImageZip(file));
    }
    @GetMapping(path = "/retrieveImage/{employeeId}")
    public byte[] retrieveImage(@PathVariable String employeeId) throws IOException {
        return employeeService.getImageByEmployeeId(employeeId);
    }
}
