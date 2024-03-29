package com.maveric.hr360.repository;

import com.maveric.hr360.entity.ReportDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDetailsRepository extends MongoRepository<ReportDetails,Long> {

    ReportDetails findBySurveyNameAndEmployeeId(String surveyId,Long employeeId);
//    ReportDetails findBySurveyId(String surveyId);

    List<ReportDetails> findBySurveyName(String surveyId);

    void deleteBySurveyName(String surveyName);
}
