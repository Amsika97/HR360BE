package com.maveric.hr360.service;


import com.maveric.hr360.entity.Employee;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Map;


public interface EmployeeDetailsService {
    Map<String, List<String>> getExcelDataAsList(File file) throws FileSystemException;

    public File convertMultiPartToPath(MultipartFile multiFile)
            throws IllegalStateException, IOException;

    Employee saveExcelData(Employee employee, List<String> responseList);


}
