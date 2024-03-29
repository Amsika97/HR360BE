package com.maveric.hr360.repository;

import com.maveric.hr360.entity.AbsoluteScore;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbsoluteScoreRepository extends MongoRepository<AbsoluteScore, Long> {

    AbsoluteScore findByEmployeeId(String employeeId);
    AbsoluteScore findByEmployeeIdAndSurveyName(String employeeId,String surveyName);
    List<AbsoluteScore> findByGrade(String grade);

    void deleteBySurveyName(String surveyName);
    List<AbsoluteScore> findByEmployeeIdIn(List<String> employeeIds);
}
