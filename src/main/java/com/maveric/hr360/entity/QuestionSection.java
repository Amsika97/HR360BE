package com.maveric.hr360.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "questionsection")
public class QuestionSection extends IdentifiedEntity {
    private String sectionName;
    private List<Weightages> weightages;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
    public QuestionSection() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

}
