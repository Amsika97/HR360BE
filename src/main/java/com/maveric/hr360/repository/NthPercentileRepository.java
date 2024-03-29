package com.maveric.hr360.repository;

import com.maveric.hr360.entity.NthPercentile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NthPercentileRepository extends MongoRepository<NthPercentile,Long> {
    void deleteBySurveyName(String surveyName);

    Optional<NthPercentile> findBySurveyNameAndNthPercent(String surveyName,int percentileValue);
}
