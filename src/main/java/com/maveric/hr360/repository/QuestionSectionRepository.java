package com.maveric.hr360.repository;

import com.maveric.hr360.entity.QuestionSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionSectionRepository extends MongoRepository<QuestionSection,Long> {
    QuestionSection findBySectionName();
}
