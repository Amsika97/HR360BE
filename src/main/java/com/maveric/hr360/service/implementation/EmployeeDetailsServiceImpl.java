package com.maveric.hr360.service.implementation;

import com.maveric.hr360.entity.Employee;

import com.maveric.hr360.repository.EmployeeRepository;
import com.maveric.hr360.service.EmployeeDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.maveric.hr360.constant.EmployeeDetailsConstant.*;
import static com.maveric.hr360.constant.SurveyDetailsConstant.DELIMITER;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeDetailsServiceImpl implements EmployeeDetailsService {
    Workbook workbook;

    private final EmployeeRepository employeeRepository;

    @Value("${tmpdir.path}")
    private String tmpDirectory;

    @Value("${header.list.employeeDetails}")
    private List<String> headerList;


    public Map<String, List<String>> getExcelDataAsList(File file) throws FileSystemException {
        log.info("employeeDetailsServiceImpl::getExcelDataAsList()::start");
        Map<String, List<String>> employeeUploadStatus = new HashMap<>();
        List<String> successRecords = new ArrayList<>();
        List<String> failedRecords = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();
        Employee employee;
        List<String> responseList = new ArrayList<>();

        try {
            workbook = WorkbookFactory.create(file);

        } catch (EncryptedDocumentException | IOException e) {
            log.debug("employeeDetailsServiceImpl::workbook creation failed");
        }

        Sheet sheet = workbook.getSheetAt(0);
        checkExcelEmpty(sheet);
        for (Row row : sheet) {
            List<String> rowList = new ArrayList<>();

            for (Cell cell : row) {
                String cellValue = dataFormatter.formatCellValue(cell);
                rowList.add(cellValue);
            }
            try {

            if(!checkValidFileByColumnHeaders(rowList,row.getRowNum())){
                throw new RuntimeException("Invalid file ");

            }

                employee = addToEmployeeData(rowList,row.getRowNum());
                if (employee != null) {
                    saveExcelData(employee, responseList);
                    successRecords.add(employee.getEmployeeId());
                }

            } catch (EncryptedDocumentException e) {
                failedRecords.add(rowList.get(0));}

            catch (RuntimeException e) {

                break;
            }

            catch (Exception e) {
                failedRecords.add(responseList.get(0));

            }

            closeWorkbook(workbook);
            log.info("employeeDetailsServiceImpl::getExcelDataAsList()::end");

        }
        employeeUploadStatus.put(ERROR, failedRecords);
        employeeUploadStatus.put(SUCCESS, successRecords);

        return employeeUploadStatus;
    }


    private static String encrypt(String plainText) {
        log.info("employeeDetailsServiceImpl::encrypt()::end");
        if (Objects.isNull(plainText)) {
            throw new IllegalArgumentException("Input is not valid");
        }
        byte[] cipherText = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            byte[] byteArray = new byte[]{92, -22, -2, 83, -122, -84, 105, -105, -40, -44, 32, -37};
            cipher.init(Cipher.ENCRYPT_MODE, stringToSecretKey(SECRET_ENCRYPTION_KEY),
                    new GCMParameterSpec(128, byteArray));
            cipherText = cipher.doFinal(plainText.getBytes(UTF_8));
        } catch (Exception ex) {
            log.debug("employeeDetailsServiceImpl:encrypt::encryption failed");
            throw new EncryptedDocumentException("Encryption Issue");
        }
        log.info("employeeDetailsServiceImpl::encrypt()::end");
        return Base64.getEncoder().encodeToString(cipherText);
    }

    private static SecretKey stringToSecretKey(String secretKey) {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, AES);
    }

    public File convertMultiPartToPath(MultipartFile multiFile)
            throws IllegalStateException, IOException {
        return convertMultiToFile(multiFile);

    }


    private  File convertMultiToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(tmpDirectory + DELIMITER + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        return file;
    }

    private Employee addToEmployeeData(List<String> encryptedList,int rowNumber)  {
        log.info("employeeDetailsServiceImpl::addToEmployeeData()::start");
        Employee employee = null;
        if (!encryptedList.isEmpty() && rowNumber!=0) {

            employee = employeeRepository.findByEmployeeId(encryptedList.get(0));
            log.debug("employeeDetailsServiceImpl::addToEmployeeData()::existing data from db getting updated");
            if (employee == null) {
                employee = new Employee();
                log.debug("employeeDetailsServiceImpl::addToEmployeeData():: data not present so creating new record");
            }

            employee.setEmployeeId(encryptedList.get(0));
            try {
                employee.setEmployeeFullName(encrypt(encryptedList.get(1)));
                employee.setBusinessPhoneNumber(encrypt(encryptedList.get(2)));
                employee.setMavericMail(encrypt(encryptedList.get(4)));
                employee.setDeliveryUnit(encryptedList.get(3));
                employee.setAccount(encryptedList.get(5));
                LocalDateTime currentTime=LocalDateTime.now(ZoneId.of("Asia/Kolkata")).withNano(0);
                employee.setCreatedAt(String.valueOf(currentTime));
                employee.setUpdatedAt(String.valueOf(currentTime));
                employee.setUpdatedBy(ADMIN);
                employee.setCreatedBy(ADMIN);
            } catch (Exception e) {
                throw new EncryptedDocumentException("Encryption failed for given value");
            }
        }
        log.info("employeeDetailsServiceImpl::addToEmployeeData()::end");
        return employee;

    }


    public Employee saveExcelData(Employee employee, List<String> responseList) {
        log.info("employeeDetailsServiceImpl::saveExcelData()::start");
        if (Objects.isNull(employee)) {
            throw new IllegalArgumentException("Input is not valid");
        }
        try {
            employee = employeeRepository.save(employee);

        } catch (Exception e) {
            log.debug("Exception occurred during save into employeeDetailsServiceImpl");
        }
        log.info("employeeDetailsServiceImpl::saveExcelData()::end");

        return employee;
    }


    public static String getDecryptedValue(String cipherText) {
        log.info("employeeDetailsServiceImpl::getDecryptedValue()::start");
        byte[] plainText = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            byte[] byteArray = new byte[]{92, -22, -2, 83, -122, -84, 105, -105, -40, -44, 32, -37};
            cipher.init(Cipher.DECRYPT_MODE, stringToSecretKey(SECRET_ENCRYPTION_KEY),
                    new GCMParameterSpec(128, byteArray));
            plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText.getBytes(UTF_8)));

        } catch (Exception ex) {
            throw new EncryptedDocumentException("Decryption failed for cipher value " + cipherText);
        }
        log.info("employeeDetailsServiceImpl::getDecryptedValue()::end");
        return new String(plainText, UTF_8);
    }

    private void closeWorkbook(Workbook workbook) {
        try {
            workbook.close();
        } catch (Exception e) {
            log.debug("employeeDetailsServiceImpl::workbook closure failed");
        }

    }
    private void checkExcelEmpty(Sheet sheet) throws FileSystemException {
        try{
            if (sheet.getRow(0) == null) {
                throw new FileSystemException("Invalid file: No data found.");
            }} catch (FileSystemException e) {
             log.debug("Invalid file: No data found.");

        }
    }
    private boolean checkValidFileByColumnHeaders(List<String> list,int rowNumber)  {
        boolean isValid = true;
        if (!list.isEmpty() && rowNumber==0) {

        if (list.size() != headerList.size()) {
            isValid = false;
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).trim().equalsIgnoreCase(headerList.get(i).trim())) {
                    isValid = false;
                    break;
                }
            }
        }



    }
        return  isValid;}


}
