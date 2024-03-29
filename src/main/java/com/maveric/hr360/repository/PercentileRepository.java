package com.maveric.hr360.repository;

import com.maveric.hr360.entity.Percentile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PercentileRepository extends MongoRepository<Percentile,Long> {
    void deleteBySurveyName(String surveyName);
}
