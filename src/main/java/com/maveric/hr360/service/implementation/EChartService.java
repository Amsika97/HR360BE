package com.maveric.hr360.service.implementation;

import com.maveric.hr360.clients.echart.ChartApiClient;
import com.maveric.hr360.clients.echart.response.EChartResponse;
import com.maveric.hr360.dto.*;
import com.maveric.hr360.entity.*;
import com.maveric.hr360.repository.AbsoluteScoreRepository;
import com.maveric.hr360.repository.NthPercentileRepository;
import com.maveric.hr360.repository.QuestionsRepository;
import com.maveric.hr360.repository.ReportDetailsRepository;
import com.maveric.hr360.service.PdfZipUploadService;
import com.maveric.hr360.utils.EChartUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.maveric.hr360.utils.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EChartService {

    private final ChartApiClient chartApiClient;
    private final ReportDetailsRepository reportDetailsRepository;
    private final PDFReportService pdfReportService;
    private final QuestionsRepository questionsRepository;
    private final AbsoluteScoreRepository absoluteScoreRepository;
    private final NthPercentileRepository percentileRepository;
    private final PdfZipUploadService pdfZipUploadService;
    @Value("${nthPercentile.values}")
    private String percentileValuesFromProperties;

    @Value("${nthPercentile.colors}")
    private String radarGraphColors;

    @Value("${output.path}")
    private String outputPath;

    @Value("${reference.percentile.list}")
    private String referencePercentile;

    public String generateChart(String surveyName,String grade) {
        List<ReportDetails> reportDetailsList = reportDetailsRepository.findBySurveyName(surveyName);
        String zipFileName = surveyName.replace("-","_").replace(":","_");
        String errorFilePath = outputPath+zipFileName+"_error.txt";

        List<String> filePaths = new ArrayList<>();
        for (ReportDetails reportDetails : reportDetailsList){
            try {
                File file = generateChartForIndividual(reportDetails.getEmployeeId(), surveyName,grade);
                filePaths.add(file.getPath());
            }catch (Exception e) {
                log.error(e.getMessage());
                writeExceptionToFile(errorFilePath,e,reportDetails.getEmployeeId());
            }
        }
        addProcessedFiles(errorFilePath, filePaths, zipFileName);
        return "Completed";
    }

    private void addProcessedFiles(String errorFilePath, List<String> filePaths, String zipFileName) {
        File file = new File(errorFilePath);
        if(file.exists()){
            filePaths.add(errorFilePath);
        }
        log.info("Adding pdf files to zip file "+ filePaths);
        pdfZipUploadService.createZipForPdfUpload(zipFileName, filePaths);
        EChartUtil.deleteFilesInFolder(outputPath);
    }

    private void writeExceptionToFile(String errorFilePath,Exception e,Long employeeid) {
        try (FileWriter writer = new FileWriter(errorFilePath, true)) {
            writer.write("Exception message for "+employeeid+": " + e.getMessage() + "\n");
        } catch (IOException ex) {
            // Handle any errors that may occur while writing to the file
            log.error("Exception occurred while adding exception message to error file: "+e.getMessage());
        }
    }

    public File generateChartForIndividual(Long employeeId, String surveyId,String grade) throws Exception {
        log.info("Generating echarts for employee {} started.",employeeId);
        List<String> data = getXAxisDetails();
        List<String> overallData = getOverallXAxisDetails();
        Map<String,String> dataItem = EChartUtil.fetchDataItem();
        List<String> overallQuestionIdList = getOverallQuestionIdList();
        ReportDetails reportDetails = reportDetailsRepository.findBySurveyNameAndEmployeeId(surveyId,employeeId);
        Map<String,byte[]> chartDetails = new HashMap<>();
        Map<String,List<String>> messageDetails = new HashMap<>();
        Map<String, Double> overallScore = new HashMap<>();
        double collectiveOverallScore = 0;
        for(QuestionsList questionsList : reportDetails.getQuestionsList()){
            Map<String,DataItem> responseMap= new HashMap<>();
            List<Response> responses = questionsList.getResponseList();
            String questionId = questionsList.getQuestionId().trim();
            if( responses.get(0).getResponseType().equalsIgnoreCase("score")) {
                EChartDto chartRequest = EChartUtil.configBarChartDetails(data,
                        getText(questionId,overallQuestionIdList));
                Series series = chartRequest.getOption().getSeries().get(0);
                if(overallQuestionIdList.contains(questionId)){
                    overallScore.put(questionsList.getQuestionId(),questionsList.getCollectiveOverallScore());
                    collectiveOverallScore = collectiveOverallScore + questionsList.getCollectiveOverallScore();
                }

                List<DataItem> dataItemList = new ArrayList<>();
                for (Response response : responses) {
                    double score = Double.parseDouble(response.getResponseScore());
                    if(score > 0) {
                        DataItem dataItem1 = DataItem.builder()
                                .value(Double.parseDouble(response.getResponseScore()))
                                .itemStyle(ItemStyle.builder()
                                        .color(dataItem.get(response.getRespondentCategory()))
                                        .build())
                                .build();
                        responseMap.put(response.getRespondentCategory(),dataItem1);

                    }else {
                        responseMap.put(response.getRespondentCategory(),null);
                    }
                }
                addDataList(responseMap, dataItemList);
                dataItemList.add(DataItem.builder()
                        .value(questionsList.getCollectiveOverallScore())
                        .itemStyle(ItemStyle.builder()
                                .color(dataItem.get(COLLECTIVE))
                                .build())
                        .build());
                series.setData(dataItemList);
                chartRequest.getOption().getSeries().set(0, series);
                generateBarChart(chartRequest, chartDetails, questionId);
            }else {
                Questions questions = questionsRepository.findByQuestionIdAndLevel(
                        questionId,grade);
                List<String> message =new LinkedList<>();
                if(questions != null){
                    for(Response response : responses){
                        consolidateMessages(response, message);
                    }
                    if(message.isEmpty()){
                        message.add("");
                    }
                    messageDetails.put(questionId.charAt(0)+"_"+questions.getMessageType(),message);
                }else {
                    throw new RuntimeException("Question details missing for "+questionId);
                }
            }
        }
        EChartDto chartRequest = EChartUtil.configBarChartDetails(overallData, "Overall Score");
        Series series = chartRequest.getOption().getSeries().get(0);
        List<DataItem> dataItemList ;
        dataItemList = overallScoreDataItem(overallScore,collectiveOverallScore,dataItem);
        series.setData(dataItemList);
        chartRequest.getOption().getSeries().set(0, series);
        generateBarChart(chartRequest, chartDetails, OVERALL);
        List<RadarDataItem> radarDataItems = getRadarDataItemDetails(surveyId,grade,employeeId);
        generateRadarChart(EChartUtil.configureRadarChartDetails(getLegendList()
                ,radarDataItems),chartDetails,"Radar");
        log.info("Generating echarts for employee {} completed.",employeeId);
        return pdfReportService.createReport(chartDetails,messageDetails, String.valueOf(employeeId),grade);
    }

    private List<String> getOverallQuestionIdList() {
        List<String> overallQuestionIdList = new ArrayList<>();
        overallQuestionIdList.add("L0");
        overallQuestionIdList.add("C0");
        overallQuestionIdList.add("D0");
        overallQuestionIdList.add("T0");
        return overallQuestionIdList;
    }

    private List<DataItem> overallScoreDataItem(Map<String,Double> overallScore,double collectiveScore,
                                                Map<String,String> dataItem){
        List<DataItem> dataItemList = new ArrayList<>();
        dataItemList.add(DataItem.builder()
                .value(overallScore.get("L0"))
                .itemStyle(ItemStyle.builder()
                        .color(dataItem.get(SELF))
                        .build()).build());
        dataItemList.add(DataItem.builder()
                .value(overallScore.get("T0"))
                .itemStyle(ItemStyle.builder()
                        .color(dataItem.get(REPORTEE))
                        .build()).build());
        dataItemList.add(DataItem.builder()
                .value(overallScore.get("C0"))
                .itemStyle(ItemStyle.builder()
                        .color(dataItem.get(PEER))
                        .build()).build());
        dataItemList.add(DataItem.builder()
                .value(overallScore.get("D0"))
                .itemStyle(ItemStyle.builder()
                        .color(dataItem.get(MANAGER))
                        .build()).build());
        dataItemList.add(DataItem.builder()
                .value(Double.parseDouble(String.format("%.1f",collectiveScore/4)))
                .itemStyle(ItemStyle.builder()
                        .color(dataItem.get(COLLECTIVE))
                        .build()).build());
        return dataItemList;
    }

    private void generateBarChart(EChartDto chartRequest, Map<String, byte[]> chartDetails, String questionId) {
        // Call the Feign client to generate the chart
        EChartResponse responseEntity = chartApiClient.generateChart(chartRequest);

        // Check if the response is successful
        validateEchartResponse(chartDetails, questionId, responseEntity);
    }

    private void generateRadarChart(RadarChartDto chartRequest, Map<String, byte[]> chartDetails, String questionId) {
        // Call the Feign client to generate the chart
        EChartResponse responseEntity = chartApiClient.generateChart(chartRequest);

        // Check if the response is successful
        validateEchartResponse(chartDetails, questionId, responseEntity);
    }

    private static void validateEchartResponse(Map<String, byte[]> chartDetails, String questionId, EChartResponse responseEntity) {
        if (responseEntity.getCode() == 200) {
            String base64Response = (String) responseEntity.getData();

            base64Response = base64Response.replace("data:image/png;base64,", "");
            // Save the base64 response to a file
            byte[] decodedBytes = Base64.getDecoder().decode(base64Response);
            chartDetails.put(questionId,decodedBytes);

        } else {
            log.info(responseEntity.getMsg());
        }
    }

    private void consolidateMessages(Response response, List<String> message){
        String responseMessage = response.getResponseMessage().trim();
        if(responseMessage.equalsIgnoreCase("nothing") ||
                responseMessage.equalsIgnoreCase("na") ||
                responseMessage.equalsIgnoreCase("n/a") ||
                responseMessage.equalsIgnoreCase("nil") ||
                responseMessage.isEmpty()
        ){
            return;
        }
        message.add(response.getResponseMessage());
    }
    private static void addDataList(Map<String,DataItem> responseMap, List<DataItem> dataItemList) {
        dataItemList.add(responseMap.get(SELF));
        dataItemList.add(responseMap.get(REPORTEE));
        dataItemList.add(responseMap.get(PEER));
        dataItemList.add(responseMap.get(MANAGER));
    }

    private static List<String> getXAxisDetails() {
        List<String> data = new ArrayList<>();
        data.add(SELF);
        data.add(REPORTEE);
        data.add(PEER);
        data.add(MANAGER);
        data.add(COLLECTIVE);
        return data;
    }

    private static List<String> getOverallXAxisDetails() {
        List<String> data = new ArrayList<>();
        data.add("DM ");
        data.add("CM ");
        data.add("TM ");
        data.add("LS ");
        data.add("Overall ");
        return data;
    }

    public String getText(String questionId,List<String> overallQuestionIdList){
        if(overallQuestionIdList.contains(questionId)){
            return switch (questionId) {
                case "L0" -> LEADERSHIP_SKILLS;
                case "C0" -> "Client Management";
                case "D0" -> "Delivery Management";
                default -> "Time Management";
            };
        }
        return questionId + " -360 degree analysis";
    }

    private List<RadarDataItem> getRadarDataItemDetails(String surveyId,String grade,Long employeeId){

        AbsoluteScore absoluteScore = absoluteScoreRepository.findByEmployeeId(String.valueOf(employeeId));

        List<RadarDataItem> radarDataItems = new ArrayList<>();

        Label label = Label.builder()
                .show(false)
                .position("right")
                .color("#000000")
                .build();
        label.setFontStyle("lighter");
        label.setFontSize(30);

        String[] percentileValues = percentileValuesFromProperties.split(",");
        String[] colors = radarGraphColors.split(",");
        int colorIndex = 0;
        for(String percentile : percentileValues) {
            Optional<NthPercentile> nthPercentileOptional = percentileRepository.findBySurveyNameAndNthPercent(
                    surveyId, Integer.parseInt(percentile));
            if(nthPercentileOptional.isPresent()) {
                NthPercentile nthPercentile = nthPercentileOptional.get();
                List<Double> percintileList = new ArrayList<>();
                percintileList.add(nthPercentile.getGrandTotal());
                percintileList.add(nthPercentile.getCustomerManagement());
                percintileList.add(nthPercentile.getLeadershipSkills());
                percintileList.add(nthPercentile.getTeamManagement());
                percintileList.add(nthPercentile.getDeliveryManagement());
                radarDataItems.add(RadarDataItem.builder()
                        .value(percintileList)
                        .itemStyle(ItemStyle.builder().color(colors[colorIndex]).build())
                        .label(label)
                        .lineStyle(LineStyle.builder().width(7).build())
                        .name(percentile+"th Percentile")
                        .build());
            }else {
                throw new RuntimeException("Percentile value is missing for "+employeeId);
            }
            colorIndex++;
        }

        Label userLabel = Label.builder()
                .show(true)
                .position("right")
                .color("#000000")
                .build();
        userLabel.setFontStyle("bold");
        userLabel.setFontSize(30);
        List<Double> percintileList = new ArrayList<>();
        percintileList.add(absoluteScore.getGrandTotal());
        percintileList.add(absoluteScore.getCustomerManagement());
        percintileList.add(absoluteScore.getLeadershipSkills());
        percintileList.add(absoluteScore.getTeamManagement());
        percintileList.add(absoluteScore.getDeliveryManagement());
        radarDataItems.add(RadarDataItem.builder()
                .value(percintileList)
                .itemStyle(ItemStyle.builder().color("#4F81BD").build())
                .lineStyle(LineStyle.builder().width(7).build())
                .label(userLabel)
                .name("Your Score")
                .build());
        referenceRadarCharts( radarDataItems);
        return radarDataItems;
    }

    private void referenceRadarCharts( List<RadarDataItem> radarDataItems) {
        String[] percentileValues = referencePercentile.split(",");
        for(String percentileValue : percentileValues) {
            double referenceValue = Double.parseDouble(percentileValue);
            Label label = Label.builder()
                    .show(true)
                    .position("inside")
                    .color("#000000")
                    .build();
            label.setFontStyle("lighter");
            label.setFontSize(20);
            List<Double> percentileList = new ArrayList<>();
            percentileList.add(referenceValue);
            percentileList.add(referenceValue);
            percentileList.add(referenceValue);
            percentileList.add(referenceValue);
            percentileList.add(referenceValue);
            radarDataItems.add(RadarDataItem.builder()
                    .value(percentileList)
                    .itemStyle(ItemStyle.builder().color("#b8b8b8").build())
                    .label(label)
                    .lineStyle(LineStyle.builder().width(4).build())
                    .build());
        }
    }

    private List<String> getLegendList(){
        List<String> legendList = new ArrayList<>();
        String[] percentileValues = percentileValuesFromProperties.split(",");
        for(String percentile : percentileValues) {
            legendList.add(percentile+"th Percentile");
        }
        legendList.add("Your Score");
        return legendList;
    }
}
