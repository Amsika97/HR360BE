package com.maveric.hr360.repository;




import com.maveric.hr360.entity.SurveyDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SurveyDetailsRepository extends MongoRepository<SurveyDetails, Long> {

    SurveyDetails findBySurveyName(String surveyName);
}
