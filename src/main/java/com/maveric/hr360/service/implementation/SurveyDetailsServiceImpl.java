package com.maveric.hr360.service.implementation;

import com.maveric.hr360.entity.EmployeesResponse;
import com.maveric.hr360.entity.Response;
import com.maveric.hr360.entity.ResponseList;
import com.maveric.hr360.entity.SurveyDetails;
import com.maveric.hr360.repository.SurveyDetailsRepository;
import com.maveric.hr360.service.ScoreCalculationService;
import com.maveric.hr360.service.SurveyDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


import static com.maveric.hr360.constant.SurveyDetailsConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyDetailsServiceImpl implements SurveyDetailsService {
    private final SurveyDetailsRepository surveyDetailsRepository;
    private final ScoreCalculationService scoreCalculationService ;
    
    Workbook workbook;
    @Value("${header.list.surveyDetails}")
    private List<String> headers;


    @Value("${tmpdir.path}")
    private String tmpDirectory;

    @Override
    public Map<String, ArrayList> getExcelDataAsList(List<File> fileList) throws IOException {
        log.info("SurveyDetailsServiceImpl::getExcelDataAsList()::Start");
        ArrayList<String> successResponseList = new ArrayList<>();
        ArrayList<String> failedResponseList = new ArrayList<>();
        Map<String, ArrayList> getExcelDataResponseMap = new HashMap<>();
        ArrayList<String> additionalDetailsList = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();
        String isFetchError;
        ArrayList<EmployeesResponse> employeesResponses = new ArrayList<>();
        ArrayList<ResponseList> listOfResponseList = null;

        for (File file : fileList) {
            try {
                workbook = WorkbookFactory.create(file);
                log.info("SurveyDetailsServiceImpl::getExcelDataAsList()::workbook created");
            } catch (EncryptedDocumentException | IOException e) {
                e.printStackTrace();
            }

            Sheet sheet = workbook.getSheetAt(0);
            List<Integer> noOfQuestionPerLevel = new ArrayList<>();
            try {
                checkExcelEmpty(sheet);
                Cell cell =null;
                for (Row row : sheet) {
                    log.info("row value {} ",row.toString());
                    List<String> rowList = new ArrayList<>();
                    listOfResponseList = new ArrayList<>();

                   /* for (Cell cell : row) {
                        log.info(cell.toString());
                        log.info(dataFormatter.formatCellValue(cell));
                        rowList.add(dataFormatter.formatCellValue(cell));
                    }*/
                    for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                        cell=row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        log.info(cell.toString());
                        log.info(dataFormatter.formatCellValue(cell));
                        rowList.add(dataFormatter.formatCellValue(cell));
                    }
                    checkIfRowIsBlank(file, rowList);

                    if (row.getRowNum()==0) {
                        noOfQuestionPerLevel = setQuestionCountPerLevel(rowList,row.getRowNum());

                    }
                    if (!rowList.isEmpty()) {

                        listOfResponseList = setResponseListDetails(rowList, listOfResponseList, noOfQuestionPerLevel,row.getRowNum());
                        EmployeesResponse employeesResponse = new EmployeesResponse();
                        if (!listOfResponseList.isEmpty()) {

                            employeesResponse.setResponsesList(listOfResponseList);
                            employeesResponse.setEmployeeId(rowList.get(0));

                            employeesResponses.add(employeesResponse);
                        }
                        log.debug("file data save successfully" + file.getName());
                        additionalDetailsList.add(0, rowList.get(2));
                    }
                }
                successResponseList.add(file.getName());

            } catch (Exception e) {
                log.error("IO exception, file fetch failed", e);
                failedResponseList.add(file.getName());
                isFetchError = TRUE;
                if (!additionalDetailsList.isEmpty()) additionalDetailsList.add(1, isFetchError);
                else additionalDetailsList.add(0, isFetchError);

            }

            closeWorkbook(workbook);
        }
        getExcelDataResponseMap.put(EMPLOYEE_RESPONSE, employeesResponses);
        getExcelDataResponseMap.put(SUCCESS, successResponseList);
        getExcelDataResponseMap.put(FAILED, failedResponseList);
        getExcelDataResponseMap.put(ADDITIONAL_DETAILS, additionalDetailsList);

        return getExcelDataResponseMap;

    }

    public SurveyDetails setSurveyDetails(Map<String, ArrayList> excelDataAsMap) {
        log.info("SurveyDetailsServiceImpl::setSurveyDetails()::start");
        if (Objects.isNull(excelDataAsMap)) {
            throw new IllegalArgumentException("Employee Response list is not valid");
        }
        LocalDateTime currentTime=LocalDateTime.now(ZoneId.of("Asia/Kolkata")).withNano(0);
        ArrayList<EmployeesResponse> employeesResponses = excelDataAsMap.get(EMPLOYEE_RESPONSE);
        ArrayList<String> additionalDetails = excelDataAsMap.get(ADDITIONAL_DETAILS);
        SurveyDetails surveyDetails = new SurveyDetails();
        surveyDetails.setSurveyName(SURVEY + currentTime);
        surveyDetails.setTemplateId(TEMPLATE_ID);
        surveyDetails.setCreatedAt(String.valueOf(currentTime));
        surveyDetails.setUpdatedAt(String.valueOf(currentTime));
        surveyDetails.setCreatedBy(ADMIN);
        surveyDetails.setUpdatedBy(ADMIN);
        surveyDetails.setGrade(additionalDetails.get(0));

        surveyDetails.setEmployeesResponses(employeesResponses);
        log.info("SurveyDetailsServiceImpl::setSurveyDetails()::end");
        return surveyDetails;
    }

    private List<Integer> setQuestionCountPerLevel(List<String> list,int rowNumber) throws FileSystemException {
        if (Objects.isNull(list)) {
            throw new IllegalArgumentException("Employee Response list is not valid");
        }
        checkValidFileByColumnHeaders(list);
        log.info("SurveyDetailsServiceImpl::setQuestionCountPerLevel()::start");
        List<String> leadershipQueList = new ArrayList<>();
        List<String> teamQueList = new ArrayList<>();
        List<String> customerQuesList = new ArrayList<>();
        List<String> deliveryList = new ArrayList<>();
        List<Integer> noOfQuestion = null;

        if (rowNumber==0) {
            for (String que : list) {
                que=que.trim();
                switch (que.charAt(0)) {
                    case 'L':
                        leadershipQueList.add(que);
                        leadershipQueList.remove(LEVEL);
                        break;
                    case 'T':
                        teamQueList.add(que);
                        break;
                    case 'C':
                        customerQuesList.add(que);
                        break;
                    case 'D':
                        deliveryList.add(que);
                        break;
                }

            }

        }

        noOfQuestion = new ArrayList<>();
        noOfQuestion.add(leadershipQueList.size());
        noOfQuestion.add(teamQueList.size());
        noOfQuestion.add(customerQuesList.size());
        noOfQuestion.add(deliveryList.size());


        log.info("SurveyDetailsServiceImpl::setQuestionCountPerLevel()::end");
        return noOfQuestion;
    }

    private ArrayList<ResponseList> setResponseListDetails(List<String> list, ArrayList<ResponseList> listOfResponseList, List<Integer> noOfQuestionPerLevel,int rowNumber) {
        log.info("SurveyDetailsServiceImpl::setResponseListDetails()::start");
        int lCounter = 1;
        int tCounter = 1;
        int cCounter = 1;
        int dCounter = 1;
        if (rowNumber!=0 && !list.isEmpty()) {
            try {
                listOfResponseList = setlistOfResponseList(list, listOfResponseList, noOfQuestionPerLevel, lCounter, tCounter, cCounter, dCounter);
            } catch (Exception exception) {
                log.error("failed storing responseList");
            }
        }
        log.info("SurveyDetailsServiceImpl::setResponseListDetails()::end");
        return listOfResponseList;
    }

    @Override
    public Map<String, ArrayList<String>> saveExcelData(Map<String, ArrayList> excelDataAsMap, SurveyDetails surveyDetails) {
        log.info("SurveyDetailsServiceImpl::saveExcelData()::start");

        Map<String, ArrayList<String>> fileUploadStatus = new HashMap<>();


        if (Objects.isNull(surveyDetails)) {
            throw new IllegalArgumentException("Input is not valid");
        }
        try {
            if (!excelDataAsMap.get(ADDITIONAL_DETAILS).contains(TRUE)) {
                surveyDetails = surveyDetailsRepository.save(surveyDetails);

                fileUploadStatus.put(SUCCESS, excelDataAsMap.get(SUCCESS));
                fileUploadStatus.put(ERROR, excelDataAsMap.get(FAILED));
            } else throw new FileSystemException("Invalid file: No data found");

            log.debug("SurveyDetailsServiceImpl::saveExcelData()::Data saved");
        } catch (FileSystemException e) {
            String failedFiles = String.valueOf(excelDataAsMap.get(FAILED)).replace(START_LIST, "").replace(END_LIST, "").concat(" failed.Please reupload all files.");
            ArrayList<String> failedList = new ArrayList<>();
            failedList.add(failedFiles);
            fileUploadStatus.put(SUCCESS, excelDataAsMap.get(SUCCESS));
            fileUploadStatus.put(ERROR, failedList);
            log.error("IO exception, file fetch failed", e);

        }
        log.info("SurveyDetailsServiceImpl::saveExcelData()::end");
        if (fileUploadStatus.get(ERROR).isEmpty()) {
            return scoreCalculationService.scoreCalculation(surveyDetails.getSurveyName());
        }
        return fileUploadStatus;
    }

    @Override
    public List<File> convertMultiPartToPath(MultipartFile[] multiFiles) throws IllegalStateException, IOException {
        log.info("SurveyDetailsServiceImpl::convertMultiPartToPath()::Start");
        List<File> files = new ArrayList<>();
        for (MultipartFile multipartFile : multiFiles) {
            files.add(convertMultiToFile(multipartFile));
        }
        log.info("SurveyDetailsServiceImpl::convertMultiPartToPath()::end");
        return files;
    }


    private File convertMultiToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(tmpDirectory + DELIMITER + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        return file;
    }

    private ArrayList<ResponseList> setlistOfResponseList(List<String> list, ArrayList<ResponseList> listOfResponseList, List<Integer> noOfQuestionPerLevel, int lCounter, int tCounter, int cCounter, int dCounter) {
        for (int i = 4; i <= list.size() - 1; i++) {

            ArrayList<Response> rList = new ArrayList<>();
            ResponseList responseList = new ResponseList();


            if (i < 4 + noOfQuestionPerLevel.get(0)) responseList.setQuestionId("L" + lCounter++);
            if (i >= 4 + noOfQuestionPerLevel.get(0) && i < (4 + noOfQuestionPerLevel.get(0) + noOfQuestionPerLevel.get(1)))
                responseList.setQuestionId("T" + tCounter++);
            if (i >= (4 + noOfQuestionPerLevel.get(0) + noOfQuestionPerLevel.get(1)) && i < (4 + noOfQuestionPerLevel.get(0) + noOfQuestionPerLevel.get(1) + noOfQuestionPerLevel.get(2)))
                responseList.setQuestionId("C" + cCounter++);
            if (i >= (4 + noOfQuestionPerLevel.get(0) + noOfQuestionPerLevel.get(1) + noOfQuestionPerLevel.get(2)) && i < (4 + noOfQuestionPerLevel.get(0) + noOfQuestionPerLevel.get(1) + noOfQuestionPerLevel.get(2) + noOfQuestionPerLevel.get(3)))
                responseList.setQuestionId("D" + dCounter++);
            Response response = new Response();
            if (list.get(i).trim().length() <= 2 && list.get(i).matches(".*\\b(?:[0-9]|10)\\b.*")) {//^[0-9]
                response.setResponseType(SCORE);
                response.setResponseScore(list.get(i).trim());
                response.setRespondentCategory(list.get(3));
                rList.add(response);
                responseList.setResponses(rList);

            } else {
                response.setResponseType(MESSAGE);
                response.setResponseMessage(list.get(i).trim());
                response.setRespondentCategory(list.get(3));
                rList.add(response);
                responseList.setResponses(rList);

            }
            listOfResponseList.add(responseList);
        }
        return listOfResponseList;
    }

    private void closeWorkbook(Workbook workbook) {
        try {
            workbook.close();
        } catch (Exception e) {
            log.debug("AbsoluteScoreDetailsServiceImpl::workbook closure failed");
        }

    }

    private void checkExcelEmpty(Sheet sheet) throws FileSystemException {
        if (sheet.getRow(0) == null) {
            throw new FileSystemException("Invalid file: No data found.");
        }
    }

    private void checkIfRowIsBlank(File file, List<String> rowList) throws FileSystemException {
        if (rowList.get(0).equalsIgnoreCase("")) {
            log.error("Invalid file {}", file.getAbsoluteFile());
            throw new FileSystemException("Invalid file: No data found.");
        }
    }

    private void checkValidFileByColumnHeaders(List<String>list) throws FileSystemException {
    boolean isValid=true;
            if (list.size() < 4) {
                throw new IllegalArgumentException("Input is not valid");
            }
            list = list.subList(0, 4);
        if (list.size() != headers.size()) {
            isValid = false;
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).trim().equalsIgnoreCase(headers.get(i).trim())) {
                    isValid = false;
                    break;
                }
            }
        }
        if (!isValid) {
            throw new FileSystemException("Invalid file: Headers not matching with expected value");
        }


    }


}

