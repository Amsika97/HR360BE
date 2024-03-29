package com.maveric.hr360.repository;

import com.maveric.hr360.entity.Questions;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionsRepository extends MongoRepository<Questions,Long> {

    List<Questions> findByQuestionIdStartingWithAndLevelAndQuestionType(
            final String questionIdPrefix, final String level,final String questionType);

    Questions findByQuestionIdAndLevel(String questionId, final String level);
}
