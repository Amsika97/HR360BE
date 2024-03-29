package com.maveric.hr360.service.implementation;

import com.maveric.hr360.Exception.CustomException;
import com.maveric.hr360.entity.*;
import com.maveric.hr360.repository.*;
import com.maveric.hr360.service.ScoreCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.maveric.hr360.constant.ScoreCalculationConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreCalculationServiceImpl implements ScoreCalculationService {

    private final SurveyDetailsRepository surveyDetailsRepository;
    private final ReportDetailsRepository reportDetailsRepository;
    private final QuestionsRepository questionsRepository;
    private final QuestionSectionRepository questionSectionRepository;
    private final AbsoluteScoreRepository absoluteScoreRepository;
    private final PercentileRepository percentileRepository;
    private final NthPercentileRepository nthPercentileRepository;
    private final EChartService eChartService;
    @Value("${nthPercentile.values}")
    private String percentileValuesFromProperties;


    @Override
    public Map<String, ArrayList<String>> scoreCalculation(String surveyName) {
        Map<String, ArrayList<String>> scoreCalculationResponse = new HashMap<>();
        ArrayList<String> successEmpIds = new ArrayList<>();
        ArrayList<String> errorEmpIds = new ArrayList<>();

        try {
            log.info("ScoreCalculationService::scorecalculationmethod::started");

            SurveyDetails surveyDetails = surveyDetailsRepository.findBySurveyName(surveyName);
            log.info("SurveyDetails {} fetched successfully for the given survey name {}", surveyDetails, surveyName);
            List<String> employeeIds = surveyDetails.getEmployeesResponses().stream()
                    .map(EmployeesResponse::getEmployeeId)
                    .distinct()
                    .toList();
            log.info("collecting distinct emp id list from surveydeatils {}", employeeIds);
            List<AbsoluteScore> absoluteScoreexisted = absoluteScoreRepository.findByEmployeeIdIn(employeeIds);
            Map<String, AbsoluteScore> newMap =absoluteScoreexisted.stream().collect(Collectors.toMap(AbsoluteScore::getEmployeeId, absoluteScore -> absoluteScore));
            for (String empId : employeeIds) {
                log.info("for empid {} reportdetails call ", empId);
                AbsoluteScore absoluteScore = !ObjectUtils.isEmpty(newMap.get(empId))?newMap.get(empId):new AbsoluteScore();
                ReportDetails reportDetails = scoreCalculationforOneEmployee(empId, surveyName, surveyDetails, errorEmpIds,absoluteScore);
                try {
                    reportDetailsRepository.save(reportDetails);
                } catch (Exception e) {
                    exceptionBlock(surveyName);
                    log.error("error while saving reportdetails for employee Id{} exception meassge {}", empId, e.getMessage());
                    throw new CustomException("exception while saving the reportDetails for empid " + empId + " Data with the survey name - " + surveyName + " is deleted from system", HttpStatus.INTERNAL_SERVER_ERROR);
                }

            }

            percentileCalulation(employeeIds, scoreCalculationResponse, surveyName, errorEmpIds, successEmpIds,surveyDetails.getGrade());
            log.info("percentilecalculation is completed for all the employees");
            calculateNthpercentile(surveyName, errorEmpIds,surveyDetails.getGrade());
            log.info("calculateNthpercentile is completed for all the employees");
            successEmpIds.add("Reports are being generated for "+ employeeIds.size()+" employees for the survey - "+surveyName);
            log.info("ScoreCalculationService::scorecalculationmethod::end");
            if(scoreCalculationResponse.get("error") == null){
                CompletableFuture.runAsync(() -> eChartService.generateChart(surveyName,surveyDetails.getGrade()));
            }
            return scoreCalculationResponse;
        } catch (Exception e) {
            exceptionBlock(surveyName);
            log.error("error in scoreCalculation method ,exception message {}", e.getMessage());
            errorEmpIds.add(e.getMessage());
            scoreCalculationResponse.put("error",errorEmpIds);
            return scoreCalculationResponse;
        }
    }


    public ReportDetails scoreCalculationforOneEmployee(String employeeId, String surveyName, SurveyDetails surveyDetails, ArrayList errorEmpId,AbsoluteScore absoluteScore) {
        log.info("scoreCalculationforOneEmployee call started");
        String grade = surveyDetails.getGrade();
        // Filter the responses by employee ID
        List<EmployeesResponse> filteredResponses = surveyDetails.getEmployeesResponses().stream()
                .filter(response -> response.getEmployeeId().equals(employeeId))
                .toList();

        // Create a map to hold the merged response lists by question ID
        Map<String, List<Response>> responseMap = new HashMap<>();
        // Iterate over each response list and merge responses for the same question ID
        filteredResponses.stream()
                .flatMap(response -> response.getResponsesList().stream())
                .forEach(responseList -> {
                    String questionId = responseList.getQuestionId();
                    if (questionId != null) {
                        List<Response> existingResponses = responseMap.get(questionId);
                        if (existingResponses == null) {
                            existingResponses = new ArrayList<>();
                            responseMap.put(questionId, existingResponses);
                        }
                        existingResponses.addAll(responseList.getResponses());
                    }
                });
        // Create the aggregated response object
        EmployeesResponse aggregatedResponse = new EmployeesResponse();
        aggregatedResponse.setEmployeeId(employeeId);
        // Create a list to hold the aggregated response lists
        List<ResponseList> aggregatedResponseLists = responseMap.entrySet().stream()
                .map(entry -> {
                    ResponseList responseList = new ResponseList();
                    responseList.setQuestionId(entry.getKey());
                    responseList.setResponses(entry.getValue());
                    return responseList;
                })
                .toList();
        // Set the aggregated response lists to the aggregatedResponse object
        aggregatedResponse.setResponsesList(aggregatedResponseLists);
        log.info("aggregated response  {}", aggregatedResponse);
        /*return aggregatedResponse;*/
        // Return the aggregated EmployeesResponse object
        return calculateReportDetails(aggregatedResponse, surveyName, grade, employeeId, absoluteScore, errorEmpId);
    }

    public ReportDetails calculateReportDetails(EmployeesResponse aggregatedResponse, String surveyName, String grade, String employeeId, AbsoluteScore absoluteScore, ArrayList<String> errorEmpId) {
        log.info("calculateReportDetails for employee Id {} is started", employeeId);
        try {
            log.info("calculateReportDetails method call started for emp {}", employeeId);
            ReportDetails reportDetails = new ReportDetails();
            reportDetails.setSurveyName(surveyName);
            reportDetails.setEmployeeId(Long.parseLong(aggregatedResponse.getEmployeeId()));

            List<QuestionsList> questionsList = new ArrayList<>();

            for (ResponseList questionResponse : aggregatedResponse.getResponsesList()) {
                List<Response> responseList1 = new ArrayList<>();
                String questionId = questionResponse.getQuestionId();
                log.debug("questionId {}", questionId);
                if (questionId != null /*&& questions.getQuestionType().equalsIgnoreCase("score")*/) {
                    Questions questions = questionsRepository.findByQuestionIdAndLevel(questionId, grade);
                    List<Response> responses = questionResponse.getResponses();
                    double totalWeightedScore = 0;
                    double totalWeightage = 0;
                    double managerScore = 0;
                    double skipLevelManagerScore=0;
                    double directManagerScore=0;
                    double skipLevelReporteeScore=0;
                    double directReporteeScore=0;
                    double peerScore = 0;
                    double reporteeScore = 0;
                    double selfScore = 0;
                    int managerCount = 0;
                    int skipLevelManagerCount=0;
                    int directManagerCount=0;
                    int skipLevelReporteeCount=0;
                    int directReporteeCount=0;
                    int peerCount = 0;
                    int reporteeCount = 0;
                    int selfCount = 0;
                    double peerWeightageScore=0;
                    double skipLevelManagerWeightageScore=0;
                    double skipLevelReporteeWeightageScore=0;
                    double directManagerWeightageScore=0;
                    double directReporteeWeightageScore=0;
                    double collectiveOverallScore=0;
                    // Calculate weighted average score for this question
                    for (Response resp : responses) {
                        if (resp.getResponseType().equalsIgnoreCase(SCORE)) {
                            // Calculate scores for respondent categories
                            switch (resp.getRespondentCategory()) {
                                case SKIP_LEVEL_MANAGER -> {
                                    skipLevelManagerScore += Double.parseDouble(resp.getResponseScore());
                                    skipLevelManagerCount++;
                                }
                                case  DIRECT_MANAGER -> {
                                    directManagerScore += Double.parseDouble(resp.getResponseScore());
                                    directManagerCount++;
                                }
                                case PEER -> {
                                    peerScore += Double.parseDouble(resp.getResponseScore());
                                    peerCount++;
                                }
                                case SKIP_LEVEL_REPOTEE -> {
                                    skipLevelReporteeScore += Double.parseDouble(resp.getResponseScore());
                                    skipLevelReporteeCount++;
                                }
                                case DIRECT_REPORTEE -> {
                                    directReporteeScore += Double.parseDouble(resp.getResponseScore());
                                    directReporteeCount++;
                                }
                                case SELF -> {
                                    selfScore += Double.parseDouble(resp.getResponseScore());
                                    selfCount++;
                                }
                                default -> {
                                }

                            }

                        } else {
                            responseList1.add(getResponseWithMessage(resp));
                        }

                    }
                    managerScore=skipLevelManagerScore+directManagerScore;
                    managerCount=skipLevelManagerCount+directManagerCount;
                    reporteeScore=skipLevelReporteeScore+directReporteeScore;
                    reporteeCount=skipLevelReporteeCount+directReporteeCount;

                    double peerWeightage = peerCount> 0 ?findWeightage(PEER,questions): 0;
                    double skipLevelManagerWeightage = skipLevelManagerCount > 0 ? findWeightage(SKIP_LEVEL_MANAGER,questions) : 0;
                    double directManagerWeightage = directManagerCount > 0 ? findWeightage(DIRECT_MANAGER,questions) : 0;
                    double skipLevelReporteeWeightage = skipLevelReporteeCount > 0 ? findWeightage(SKIP_LEVEL_REPOTEE,questions) : 0;
                    double directReporteeWeightage = directReporteeCount > 0 ? findWeightage(DIRECT_REPORTEE,questions) : 0;

                    peerWeightageScore=peerCount>0?(peerScore/peerCount)*peerWeightage:0;
                    skipLevelManagerWeightageScore=skipLevelManagerCount>0?(skipLevelManagerScore/skipLevelManagerCount)*skipLevelManagerWeightage:0;
                    directManagerWeightageScore=directManagerCount>0?(directManagerScore/directManagerCount)* directManagerWeightage:0;
                    skipLevelReporteeWeightageScore=skipLevelReporteeCount >0 ?(skipLevelReporteeScore/skipLevelReporteeCount)*skipLevelReporteeWeightage:0;
                    directReporteeWeightageScore=directReporteeCount>0?(directReporteeScore/directReporteeCount)* directReporteeWeightage:0;

                    totalWeightedScore=peerWeightageScore+skipLevelManagerWeightageScore+directManagerWeightageScore+skipLevelReporteeWeightageScore+directReporteeWeightageScore;
                    totalWeightage = peerWeightage+skipLevelManagerWeightage+directManagerWeightage+skipLevelReporteeWeightage+directReporteeWeightage;
                    // Calculate collective overall score for this question
                    // Calculate collectiveOverallScore and round to one decimal place inline
                    if(skipLevelManagerCount==0 || skipLevelReporteeCount==0||directManagerCount==0||directReporteeCount==0||peerCount==0) {
                        collectiveOverallScore = totalWeightage != 0 ? Double.parseDouble(String.format("%.1f", totalWeightedScore / totalWeightage)) : 0;
                        log.info("For questionId {},collective score {} ",questionId,collectiveOverallScore);
                    }else {
                        collectiveOverallScore = Double.parseDouble(String.format("%.1f",totalWeightedScore));
                        log.info("For questionId {},collective score {} ",questionId,collectiveOverallScore);
                    }
                    log.info("collectiveOverallScore {} for questionId {} ", collectiveOverallScore, questionId);
                    // Calculate averages for different respondent categories
                    double averageManagerScore = managerCount != 0 ? Double.parseDouble(String.format("%.1f" ,managerScore / managerCount)) : 0;
                    log.info("averageManagerScore {} for questionId {}", averageManagerScore, questionId);
                    double averagePeerScore = peerCount != 0 ? Double.parseDouble(String.format("%.1f", peerScore / peerCount )): 0;
                    log.info("averagePeerScore {} for questionId {}", averagePeerScore, questionId);
                    double averageReporteeScore = reporteeCount != 0 ? Double.parseDouble(String.format("%.1f", reporteeScore / reporteeCount )): 0;
                    log.info("averageReporteeScore {} for questionId {}", averageReporteeScore, questionId);
                    double averageSelfScore = selfCount != 0 ? Double.parseDouble(String.format("%.1f", selfScore / selfCount )): 0;
                    log.info("averageSelfScore {} for questionId {}", averageSelfScore, questionId);
                    List<Response> responseList2 = new ArrayList<>();
                    if (collectiveOverallScore != 0) {
                        log.info("call populateResponseList for questionId {}", questionId);
                        responseList2 = populateResponseList(averageManagerScore, averageSelfScore, averagePeerScore, averageReporteeScore);
                        log.info("resposeList {}", responseList2);
                    }
                    List<Response> finalResponseList = new ArrayList<>(responseList2);
                    finalResponseList.addAll(responseList1);
                    log.info("finalResponseList {} for questionId {}", finalResponseList, questionId);
                    QuestionsList question = new QuestionsList();
                    question.setQuestionId(questionId);
                    question.setQuestionText(questions.getQuestion());
                    question.setCollectiveOverallScore(collectiveOverallScore);
                    question.setResponseList(finalResponseList);
                    questionsList.add(question);
                }//question id
            }//1st for loop
            log.info("loop for ALl questions completed");
            log.info("call for calculateOverallSectionWise");
            questionsList.add(calculateOverallSectionWise(L0, questionsList, L, employeeId, absoluteScore, surveyName, errorEmpId,grade));
            questionsList.add(calculateOverallSectionWise(T0, questionsList, T, employeeId, absoluteScore, surveyName, errorEmpId,grade));
            questionsList.add(calculateOverallSectionWise(D0, questionsList, D, employeeId, absoluteScore, surveyName, errorEmpId,grade));
            questionsList.add(calculateOverallSectionWise(C0, questionsList, C, employeeId, absoluteScore, surveyName, errorEmpId,grade));
            reportDetails.setQuestionsList(questionsList);
            return reportDetails;
        } catch (Exception e) {
            log.error("Failed to calculate report details for employee ID {} and delete the entries from reportdetails,absolutescore,percentile and Nth percentile, exception message {}", employeeId, e.getMessage());
            exceptionBlock(surveyName);
            throw new CustomException(ERROR_MESSAGE + employeeId , HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    private QuestionsList calculateOverallSectionWise(String specialCaseId, List<QuestionsList> questionsList, String prefix, String employeeId, AbsoluteScore absoluteScore, String surveyName, ArrayList<String> errorEmpId,String grade) {
        try {
            log.info("calculateOverallSectionWise method call started for empId {}", employeeId);
            double totalScore = 0;
            int count = 0;
            double overallManagerScore = 0.0;
            double overallManagerScoreCount = 0.0;
            double overallPeerScore = 0.0;
            double overallPeerScoreCount = 0.0;
            double overallSelfScore = 0.0;
            double overallSelfScoreCount = 0.0;
            double overallReporteeScore = 0.0;
            double overallReporteeScoreCount = 0.0;
            /*double collectiveOverallScore=0.0;*/
            for (QuestionsList question : questionsList) {

                String questionId = question.getQuestionId();
                if (questionId.startsWith(prefix)) {
                    for (Response response : question.getResponseList()) {
                        if (response.getResponseType().equalsIgnoreCase("Score")) {
                            switch (response.getRespondentCategory()) {
                                case MANAGER -> {
                                    overallManagerScore += Double.parseDouble(response.getResponseScore());
                                    overallManagerScoreCount++;
                                }
                                case PEER -> {
                                    overallPeerScore += Double.parseDouble(response.getResponseScore());
                                    overallPeerScoreCount++;
                                }
                                case REPORTEE -> {
                                    overallReporteeScore += Double.parseDouble(response.getResponseScore());
                                    overallReporteeScoreCount++;
                                }
                                case SELF -> {
                                    overallSelfScore += Double.parseDouble(response.getResponseScore());
                                    overallSelfScoreCount++;
                                }
                                default -> {
                                }
                            }
                        }
                    }
                    totalScore += question.getCollectiveOverallScore();
                    if (question.getCollectiveOverallScore() != 0) {
                        count++;
                    }
                }
            }
            double collectiveOverallScore = Double.parseDouble(String.format("%.1f", totalScore / count));
            log.info("colectiveOverallScore for {}", specialCaseId);
            double averageOverallManagerScore = overallManagerScoreCount != 0 ? Double.parseDouble(String.format("%.1f", overallManagerScore / overallManagerScoreCount)) : 0;
            double averageOverallPeerScore = overallPeerScoreCount != 0 ? Double.parseDouble(String.format("%.1f", overallPeerScore / overallPeerScoreCount)) : 0;
            double averageOverallReporteeScore = overallReporteeScoreCount != 0 ? Double.parseDouble(String.format("%.1f",overallReporteeScore / overallReporteeScoreCount )): 0;
            double averageOverallSelfScore = overallSelfScoreCount != 0 ? Double.parseDouble(String.format("%.1f", overallSelfScore / overallSelfScoreCount)) : 0;
            QuestionsList specialCaseQuestion = new QuestionsList();
            specialCaseQuestion.setQuestionId(specialCaseId);
            specialCaseQuestion.setCollectiveOverallScore(collectiveOverallScore);
            List<Response> responseListForOverall = populateResponseList(averageOverallManagerScore, averageOverallSelfScore, averageOverallPeerScore, averageOverallReporteeScore);
            specialCaseQuestion.setResponseList(responseListForOverall);
            absoluteScoreRepository.save(populateAbsoluteScore(surveyName,collectiveOverallScore, specialCaseId, employeeId, absoluteScore,grade));
            log.info("absoluteScore is saved successfully");
            return specialCaseQuestion;
        } catch (Exception e) {
            errorEmpId.add(employeeId);
            log.error("Failed to calculateOverallSectionWise for employee ID {} and delete the entries from reportdetails,absolutescore,percentile and Nth percentile, exception message {}", employeeId, e.getMessage());
            exceptionBlock(surveyName);
            throw new CustomException(ERROR_MESSAGE + employeeId , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Response> populateResponseList(Double averageManagerScore, Double averageSelfScore, Double averagePeerScore, Double averageReporteeScore) {
        List<Response> responseList = new ArrayList<>();
        Response managerResponse = getResponse(MANAGER, averageManagerScore);
        Response peerResponse = getResponse(PEER, averagePeerScore);
        Response reporteeResponse = getResponse(REPORTEE, averageReporteeScore);
        Response selfResponse = getResponse(SELF, averageSelfScore);
        responseList.add(managerResponse);
        responseList.add(peerResponse);
        responseList.add(reporteeResponse);
        responseList.add(selfResponse);
        return responseList;
    }

    private Response getResponse(String value, double averageScore) {
        Response response = new Response();
        response.setRespondentCategory(value);
        response.setResponseScore(String.valueOf(averageScore));
        response.setResponseType(SCORE);
        return response;
    }

    private Response getResponseWithMessage(Response resp) {
        Response response1 = new Response();
        response1.setResponseMessage(resp.getResponseMessage());
        response1.setResponseType(resp.getResponseType());
        response1.setRespondentCategory(resp.getRespondentCategory());
        return response1;
    }

    private AbsoluteScore populateAbsoluteScore(String surveyName,Double collectiveScore, String specialcaseId, String employeeId, AbsoluteScore absoluteScore,String grade) {
        log.info("populateAbsoluteScore for for empId{} specailcase id {}", employeeId, specialcaseId);
        switch (specialcaseId) {
            case L0 -> absoluteScore.setLeadershipSkills(collectiveScore);
            case T0 -> absoluteScore.setTeamManagement(collectiveScore);
            case C0 -> absoluteScore.setCustomerManagement(collectiveScore);
            case D0 -> absoluteScore.setDeliveryManagement(collectiveScore);
            default -> {
            }
        }
        double grandCollective = Double.parseDouble(String.format("%.1f",(absoluteScore.getLeadershipSkills() + absoluteScore.getCustomerManagement() + absoluteScore.getDeliveryManagement() + absoluteScore.getTeamManagement()) / 4));
        absoluteScore.setEmployeeId(employeeId);
        absoluteScore.setGrandTotal(grandCollective);
        absoluteScore.setGrade(grade);
        absoluteScore.setSurveyName(surveyName);
        return absoluteScore;
    }


    //find weightage for question
    private double findWeightage(String respondentCategory,Questions questions) {

        Long id = questions.getQuestionSection().getId();
        Optional<QuestionSection> questionSection = questionSectionRepository.findById(id);
        if (questionSection.isPresent()) {
            List<Weightages> weightages = questionSection.get().getWeightages();
            for (Weightages weightage : weightages) {
                if (weightage.getRespondentCategory().equals(respondentCategory)) {
                    return weightage.getWeightage();
                }
            }
        }
        return ZERO;
    }

    public Map<String, ArrayList<String>> percentileCalulation(List<String> employeeIds, Map<String, ArrayList<String>> scoreCalculationResponse, String surveyName, ArrayList<String> errorEmpId,ArrayList<String> successEmpId,String grade) {
        log.info("percentileCalulation method call started");
        try {
            Percentile percentile = new Percentile();
            List<AbsoluteScore> absoluteScoreList = absoluteScoreRepository.findByGrade(grade);
            List<Map<String, String>> sortedDeliveryManagementList = collectScoreListSorted(absoluteScoreList, DELIVERYMANAGEMENT);
            log.info("sorted deliverymanagement {}", sortedDeliveryManagementList);
            List<Map<String, String>> sortedLeadershipSkillsList = collectScoreListSorted(absoluteScoreList, LEADERSHIPSKILLS);
            log.info("sorted leadershipSkills {}", sortedLeadershipSkillsList);
            List<Map<String, String>> sortedTeamManagementList = collectScoreListSorted(absoluteScoreList, TEAMMANAGEMENT);
            log.info(" sortedTeamManagementList {}", sortedTeamManagementList);
            List<Map<String, String>> sortedCustomerManagementList = collectScoreListSorted(absoluteScoreList, CUSTOMERMANAGEMENT);
            log.info("sortedCustomerManagementList {}", sortedCustomerManagementList);
            List<Map<String, String>> sortedGrandTotalList = collectScoreListSorted(absoluteScoreList, GRANDTOTAL);
            log.info("sortedGrandTotalList {}", sortedGrandTotalList);

            for (String emp : employeeIds) {
                percentile = calculatePercentile(sortedDeliveryManagementList, sortedLeadershipSkillsList, sortedTeamManagementList, sortedCustomerManagementList, sortedGrandTotalList, emp,surveyName);
                log.info("percentile {} for empid {}", percentile, emp);
                try {
                    percentileRepository.save(percentile);
                    /*successEmpId.add(emp);*/
                } catch (Exception e) {
                    errorEmpId.add(emp);
                    exceptionBlock(surveyName);
                    log.error("Failed to save percentile for employee ID {} exception message {}", emp, e.getMessage());
                }
            }
            scoreCalculationResponse.put("success", successEmpId);
            log.info("percentile calculated successfully");
            return scoreCalculationResponse;
        } catch (Exception e) {
            log.error("An error occurred while calculating percentiles", e);
            exceptionBlock(surveyName);
            errorEmpId.add(e.getMessage());
            scoreCalculationResponse.put("error",errorEmpId);
            return scoreCalculationResponse;
        }
    }

    private List<Map<String, String>> collectScoreListSorted(List<AbsoluteScore> absoluteScoreList, String attributeName) {
        return absoluteScoreList.stream()
                .map(score -> Map.of(
                        EMPLOYEEID, score.getEmployeeId(),
                        attributeName, String.valueOf(getAttributeValue(score, attributeName))
                ))
                .sorted(Comparator.comparingDouble(map -> Double.parseDouble(map.get(attributeName))))
                .toList();
    }

    private Double getAttributeValue(AbsoluteScore score, String attributeName) {
        return switch (attributeName) {
            case CUSTOMERMANAGEMENT -> score.getCustomerManagement();
            case DELIVERYMANAGEMENT -> score.getDeliveryManagement();
            case LEADERSHIPSKILLS -> score.getLeadershipSkills();
            case TEAMMANAGEMENT -> score.getTeamManagement();
            case GRANDTOTAL -> score.getGrandTotal();
            default -> ZERO;
        };
    }

    private Percentile calculatePercentile(List<Map<String, String>> sortedDeliveryManagementList,
                                           List<Map<String, String>> sortedLeadershipSkillsList,
                                           List<Map<String, String>> sortedTeamManagementList,
                                           List<Map<String, String>> sortedCustomerManagementList,
                                           List<Map<String, String>> sortedGrandTotalList,
                                           String emp,String surveyName) {
        log.info("calculatePercentile for emp {} call started", emp);
        double deliveryManagementPercentile = calculatePercentileForEmployee(sortedDeliveryManagementList, emp,surveyName);
        log.info("deliveryManagementPercentile {} for emp {}", deliveryManagementPercentile, emp);
        double leadershipSkillsPercentile = calculatePercentileForEmployee(sortedLeadershipSkillsList, emp,surveyName);
        log.info("leadershipSkillsPercentile {} for emp {}", leadershipSkillsPercentile, emp);
        double teamManagementPercentile = calculatePercentileForEmployee(sortedTeamManagementList, emp,surveyName);
        log.info("teamManagementPercentile {} for emp {}", teamManagementPercentile, emp);
        double customerManagementPercentile = calculatePercentileForEmployee(sortedCustomerManagementList, emp,surveyName);
        log.info("customerManagementPercentile {} for emp {}", customerManagementPercentile, emp);
        double grandTotalPercentile = calculatePercentileForEmployee(sortedGrandTotalList, emp,surveyName);
        log.info("grandTotalPercentile {} for emp {}", grandTotalPercentile, emp);
        Percentile percentile = new Percentile();
        percentile.setEmployeeId(emp);
        percentile.setSurveyName(surveyName);
        percentile.setTeamManagement(teamManagementPercentile);
        percentile.setCustomerManagement(customerManagementPercentile);
        percentile.setDeliveryManagement(deliveryManagementPercentile);
        percentile.setGrandTotal(grandTotalPercentile);
        percentile.setLeadershipSkills(leadershipSkillsPercentile);
        return percentile;

    }

    private double calculatePercentileForEmployee(List<Map<String, String>> sortedList, String emp,String surveyName) {
        try {
            double percentile = 0.0;

            for (int i = 0; i < sortedList.size(); i++) {
                Map<String, String> entry = sortedList.get(i);
                String employeeId = entry.get(EMPLOYEEID);

                if (employeeId.equals(emp)) {
                    percentile = (i) / ((double) sortedList.size() - 1) * 100;
                    break;
                }
            }
            return Double.parseDouble(String.format("%.1f",percentile));
        } catch (Exception e) {
            log.error(" error at calculatePercentileForEmployee method for empployye {}",emp);
            exceptionBlock(surveyName);
            throw new CustomException(ERROR_MESSAGE + emp , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /* public List<String> getPercentileValues() {
         List<String> percentileValues= Arrays.asList(percentileValuesFromProperties.split(","));
         return percentileValues;
     }*/
    public void calculateNthpercentile(String surveyName, ArrayList<String> errorEmpId,String grade) {
        String[] percentileValues = percentileValuesFromProperties.split(",");
        for (String nthPercentile : percentileValues) {
            calculatePercentileNew(Integer.parseInt(nthPercentile), surveyName,grade);
        }
    }

    public NthPercentile calculatePercentileNew(int nthPercent, String surveyname,String grade) {
        log.info("calculatePercentileNew call started for nth precent {}", nthPercent);
        try {
            List<AbsoluteScore> absoluteScoreList = absoluteScoreRepository.findByGrade(grade);
            List<Map<String, String>> sortedDeliveryManagementList = collectScoreListSorted(absoluteScoreList, DELIVERYMANAGEMENT);
            List<Map<String, String>> sortedLeadershipSkillsList = collectScoreListSorted(absoluteScoreList, LEADERSHIPSKILLS);
            List<Map<String, String>> sortedTeamManagementList = collectScoreListSorted(absoluteScoreList, TEAMMANAGEMENT);
            List<Map<String, String>> sortedCustomerManagementList = collectScoreListSorted(absoluteScoreList, CUSTOMERMANAGEMENT);
            List<Map<String, String>> sortedGrandTotalList = collectScoreListSorted(absoluteScoreList, GRANDTOTAL);

            double deliveryManagementPercentile = calculatePercentileForEachNthvalue(sortedDeliveryManagementList, nthPercent, DELIVERYMANAGEMENT, surveyname);
            double leadershipSkillsPercentile = calculatePercentileForEachNthvalue(sortedLeadershipSkillsList, nthPercent, LEADERSHIPSKILLS, surveyname);
            double teamManagementPercentile = calculatePercentileForEachNthvalue(sortedTeamManagementList, nthPercent, TEAMMANAGEMENT, surveyname);
            double customerManagementPercentile = calculatePercentileForEachNthvalue(sortedCustomerManagementList, nthPercent, CUSTOMERMANAGEMENT, surveyname);
            double grandTotalPercentile = calculatePercentileForEachNthvalue(sortedGrandTotalList, nthPercent, GRANDTOTAL, surveyname);
            NthPercentile percentile2 = new NthPercentile();
            percentile2.setTeamManagement(teamManagementPercentile);
            percentile2.setCustomerManagement(customerManagementPercentile);
            percentile2.setDeliveryManagement(deliveryManagementPercentile);
            percentile2.setGrandTotal(grandTotalPercentile);
            percentile2.setLeadershipSkills(leadershipSkillsPercentile);
            percentile2.setNthPercent(nthPercent);
            percentile2.setSurveyName(surveyname);
            nthPercentileRepository.save(percentile2);
            log.info("nth percentile {} scores is saved on DB", nthPercent);
            return percentile2;
        } catch (Exception e) {
            exceptionBlock(surveyname);
            log.error("error in calculatePercentileNew for nthpercentilevalue {} ,exception message {}", nthPercent, e.getMessage());
            throw new CustomException(NthPercentile_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Double calculatePercentileForEachNthvalue(List<Map<String, String>> sortedList, int percentileValue, String attributeName, String surveyname) {
        log.info("calculatePercentileForEachNthvalue method call started");
        try {
            double rankValue = ((double) percentileValue / 100) * sortedList.size();
            int rank = (int) rankValue;
            double fraction = rankValue - rank;
            Map<String, String> entry = sortedList.get(rank - 1);

            double value = Double.parseDouble(entry.get(attributeName));
            double nextHeighestValue = 0.0;
            int nextHeighestRank = 0;

            for (int i = rank; i < sortedList.size(); i++) {
                Map<String, String> nextEntry = sortedList.get(i - 1);
                double nextValue = Double.parseDouble(nextEntry.get(attributeName));
                if (nextValue > value) {
                    nextHeighestValue = nextValue;
                    nextHeighestRank = i + 1;
                } else {
                    nextHeighestValue = value;
                    nextHeighestRank = i + 1;
                }
            }
            double valueDifference = nextHeighestValue - value;
            int rankDifference = nextHeighestRank - rank;
            double decimal = fraction / rankDifference;
            return Double.parseDouble(String.format("%.1f",value + (valueDifference * decimal)));
        } catch (Exception e) {
            exceptionBlock(surveyname);
            log.error("error in calculatePercentileForEachNthvalue for nthpercentilevalue {} ,exception message {}", percentileValue, e.getMessage());
            throw new CustomException(NthPercentile_ERROR_MESSAGE , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void exceptionBlock(String surveyName) {
        reportDetailsRepository.deleteBySurveyName(surveyName);
        absoluteScoreRepository.deleteBySurveyName(surveyName);
        reportDetailsRepository.deleteBySurveyName(surveyName);
        nthPercentileRepository.deleteBySurveyName(surveyName);
    }
}




