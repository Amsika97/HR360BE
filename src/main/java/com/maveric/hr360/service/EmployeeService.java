package com.maveric.hr360.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public interface EmployeeService {
    Map<String, ArrayList<String>>saveImageZip(MultipartFile file) throws IOException;

    byte[] getImageByEmployeeId(String employeeId) throws IOException;
}
