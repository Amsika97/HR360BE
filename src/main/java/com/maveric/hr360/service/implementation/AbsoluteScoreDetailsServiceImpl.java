package com.maveric.hr360.service.implementation;

import com.maveric.hr360.entity.AbsoluteScore;
import com.maveric.hr360.entity.Percentile;
import com.maveric.hr360.repository.AbsoluteScoreRepository;
import com.maveric.hr360.repository.PercentileRepository;
import com.maveric.hr360.service.AbsoluteScoreDetailsService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.maveric.hr360.constant.AbsoluteScoreConstant.*;
import static com.maveric.hr360.constant.SurveyDetailsConstant.DELIMITER;

@Service
@Slf4j
@RequiredArgsConstructor
public class AbsoluteScoreDetailsServiceImpl implements AbsoluteScoreDetailsService {
    Workbook workbook;


    private final AbsoluteScoreRepository absoluteScoreRepository;


    private final PercentileRepository percentileRepository;

    @Value("${header.list}")
    private String[] headers;

    @Value("${header.list}")
    private List<String> headerList;

    @Value("${tmpdir.path}")
    private String tmpDirectory;


    public Map<String, String> getExcelDataAsList(File file) {

        log.info("AbsoluteScoreDetailsServiceImpl::getExcelDataAsList()::start");
        List<AbsoluteScore> absoluteScoreList=new ArrayList<>();
        Map<String, String> responseMap = new HashMap<>();
        DataFormatter dataFormatter = new DataFormatter();
        AbsoluteScore absoluteScore;

        // Create the Workbook
        try {
            workbook = WorkbookFactory.create(file);

        } catch (EncryptedDocumentException | IOException e) {
            log.debug("AbsoluteScoreDetailsServiceImpl::workbook creation failed");
        }

        // Getting the Sheet at  zero
        Sheet sheet = workbook.getSheetAt(0);
        try {
            if (sheet.getRow(0) == null) {
                throw new FileSystemException("Invalid file: No data found.");
            }

            for (Row row : sheet) {
                List<String> rowList = new ArrayList<>();

                for (Cell cell : row) {
                    String cellValue = dataFormatter.formatCellValue(cell);
                    rowList.add(cellValue);
                }


                if (rowList.get(0).equalsIgnoreCase("")) {
                    log.error("Invalid file {}", file.getAbsoluteFile());
                    throw new FileSystemException("Invalid file: No data found.");
                }
                if(!checkValidFileByColumnHeaders(rowList,row.getRowNum())){
                    log.info("isvalidmethod");
                    throw new RuntimeException("Invalid file");

                }
                absoluteScore = addToAbsoluteScoreData(rowList,row.getRowNum());

                if (absoluteScore != null)
                    responseMap = saveExcelData(absoluteScore, file,absoluteScoreList);

            }
            absoluteScoreRepository.saveAll(absoluteScoreList);
            closeWorkbook(workbook);
        }
        catch (RuntimeException e) {
            log.info("Exception occurred during save into AbsoluteScoreDetailsServiceImpl {}",e.getMessage());
            responseMap.put(MESSAGE, "Invalid file format ".concat(file.getAbsoluteFile().getName()));
        }
        catch (Exception e) {
            log.debug("Exception occurred during save into AbsoluteScoreDetailsServiceImpl {}",e.getMessage());
            responseMap.put(MESSAGE, "Failed storing file ".concat(file.getAbsoluteFile().getName()));
        }
        log.info("AbsoluteScoreDetailsServiceImpl::getExcelDataAsList()::end");
        return responseMap;
    }


    public File convertMultiPartToPath(MultipartFile multiFile)
            throws IllegalStateException, IOException {
        return convertMultiToFile(multiFile);

    }


    private File convertMultiToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(tmpDirectory + DELIMITER + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        return file;
    }

    private AbsoluteScore addToAbsoluteScoreData(List<String> encryptedList,int rowNumber) throws FileSystemException {
        log.info("AbsoluteScoreDetailsServiceImpl::addToAbsoluteScoreData()::start");
        AbsoluteScore absoluteScore = null;
        if (!encryptedList.isEmpty() && rowNumber!=0) {

            absoluteScore = absoluteScoreRepository.findByEmployeeId(encryptedList.get(0));
            log.debug("AbsoluteScoreDetailsServiceImpl::addToAbsoluteScoreData()::existing data from db getting updated");
            if (absoluteScore == null) {
                absoluteScore = new AbsoluteScore();
                log.debug("AbsoluteScoreDetailsServiceImpl::addToAbsoluteScoreData():: data not present so creating new record");
            }
            absoluteScore.setEmployeeId(encryptedList.get(0));
            absoluteScore.setCustomerManagement(Double.parseDouble(encryptedList.get(1)));
            absoluteScore.setDeliveryManagement(Double.parseDouble(encryptedList.get(2)));
            absoluteScore.setLeadershipSkills(Double.parseDouble(encryptedList.get(3)));
            absoluteScore.setTeamManagement(Double.parseDouble(encryptedList.get(4)));
            absoluteScore.setGrandTotal(Double.parseDouble(encryptedList.get(5)));
            absoluteScore.setGrade(encryptedList.get(6));

        }
        log.info("AbsoluteScoreDetailsServiceImpl::addToAbsoluteScoreData()::end");
        return absoluteScore;

    }


    public Map<String, String> saveExcelData(AbsoluteScore absoluteScore, File file,List<AbsoluteScore> absoluteScoreList) {
        Map<String, String> responseMap = new HashMap<>();
        log.info("AbsoluteScoreDetailsServiceImpl::saveExcelData()::start");
        if (Objects.isNull(absoluteScore)) {
            throw new IllegalArgumentException("Input is not valid");
        }
        try {
            absoluteScoreList.add(absoluteScore);
            /*absoluteScoreRepository.save(absoluteScore);*/
        } catch (Exception e) {
            log.info("Exception occurred during save into AbsoluteScoreDetailsServiceImpl");
            responseMap.put(MESSAGE, "Failed storing file ".concat(file.getAbsoluteFile().getName()));
            return responseMap;
        }
        log.info("AbsoluteScoreDetailsServiceImpl::saveExcelData()::end");
        responseMap.put(MESSAGE, "Data stored from file ".concat(file.getAbsoluteFile().getName()));
        return responseMap;
    }


    private List<AbsoluteScore> getAllScores() {
        return absoluteScoreRepository.findAll();
    }

    private List<Percentile> getAllPercentiles() {
        return percentileRepository.findAll();
    }


    private void formAbsoluteScoreExcelData(List<AbsoluteScore> fetchedList, HttpServletResponse response) throws IOException {
        log.info("AbsoluteScoreDetailsServiceImpl::formAbsoluteScoreExcelData()::start");
        if (fetchedList.isEmpty()) {
            throw new IllegalArgumentException("No data in database");
        }
        LocalDate currentTime = LocalDate.now((ZoneId.of("Asia/Kolkata")));
        String downloadedFileName = "absoluteScore_".concat(currentTime.toString()).concat(".xlsx");
        Workbook absoluteScoreWorkbook = new XSSFWorkbook();
        Sheet sheet = absoluteScoreWorkbook.createSheet(ABSOLUTE_SCORE);
        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        int cellNum = 0;
        for (String header : headers) {
            Cell cell = headerRow.createCell(cellNum++);
            cell.setCellValue(header);
        }
        for (AbsoluteScore eachRow : fetchedList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(eachRow.getSurveyName());
            row.createCell(1).setCellValue(eachRow.getEmployeeId());
            row.createCell(2).setCellValue(eachRow.getCustomerManagement());
            row.createCell(3).setCellValue(eachRow.getDeliveryManagement());
            row.createCell(4).setCellValue(eachRow.getLeadershipSkills());
            row.createCell(5).setCellValue(eachRow.getTeamManagement());
            row.createCell(6).setCellValue(eachRow.getGrandTotal());
            row.createCell(7).setCellValue(eachRow.getGrade());
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + downloadedFileName);
        absoluteScoreWorkbook.write(response.getOutputStream());
        closeWorkbook(absoluteScoreWorkbook);
        log.info("AbsoluteScoreDetailsServiceImpl::formAbsoluteScoreExcelData()::end");
    }


    public void downloadFile(HttpServletResponse response, String tableName) throws IOException {
        log.info("AbsoluteScoreDetailsServiceImpl::downloadFile()::start");
        try {
            if (tableName.equalsIgnoreCase(ABSOLUTE_SCORE)) {
                List<AbsoluteScore> absoluteScoreList = getAllScores();
                formAbsoluteScoreExcelData(absoluteScoreList, response);
            } else {
                List<Percentile> percentileList = getAllPercentiles();
                formPercentileExcelData(percentileList, response);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No data for requested table");

        }

        log.info("AbsoluteScoreDetailsServiceImpl::downloadFile()::end");
    }

    private void formPercentileExcelData(List<Percentile> fetchedList, HttpServletResponse response) throws IOException {
        log.info("AbsoluteScoreDetailsServiceImpl::formPercentileExcelData()::start");
        if (fetchedList.isEmpty()) {
            throw new IllegalArgumentException("No data in database");
        }
        Workbook percentileWorkbook = new XSSFWorkbook();
        LocalDate currentTime = LocalDate.now((ZoneId.of("Asia/Kolkata")));
        String downloadedFileName = "percentile_".concat(currentTime.toString()).concat(".xlsx");
        Sheet sheet = percentileWorkbook.createSheet(PERCENTILE);
        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        int cellNum = 0;
        for (String header : headers) {
            Cell cell = headerRow.createCell(cellNum++);
            cell.setCellValue(header);
        }
        for (Percentile eachRow : fetchedList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(eachRow.getSurveyName());
            row.createCell(1).setCellValue(eachRow.getEmployeeId());
            row.createCell(2).setCellValue(eachRow.getCustomerManagement());
            row.createCell(3).setCellValue(eachRow.getDeliveryManagement());
            row.createCell(4).setCellValue(eachRow.getLeadershipSkills());
            row.createCell(5).setCellValue(eachRow.getTeamManagement());
            row.createCell(6).setCellValue(eachRow.getGrandTotal());
            row.createCell(7).setCellValue(eachRow.getGrade());
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + downloadedFileName);
        percentileWorkbook.write(response.getOutputStream());
        closeWorkbook(percentileWorkbook);
        log.info("AbsoluteScoreDetailsServiceImpl::formPercentileExcelData()::end");
    }

    private void closeWorkbook(Workbook workbook) {
        try {
            workbook.close();
        } catch (Exception e) {
            log.debug("AbsoluteScoreDetailsServiceImpl::workbook closure failed");
        }
    }

    private boolean checkValidFileByColumnHeaders(List<String> list,int rowNumber)  {
        boolean isValid = true;
        List<String> newHeaderList=headerList;
        if (!list.isEmpty() && rowNumber==0) {
            newHeaderList = newHeaderList.subList(1, 8);
            if (list.size() != newHeaderList.size()) {
                isValid = false;
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (!list.get(i).trim().equalsIgnoreCase(newHeaderList.get(i).trim())) {
                        isValid = false;
                        break;
                    }
                }
            }
        }
        return isValid;
    }
}


