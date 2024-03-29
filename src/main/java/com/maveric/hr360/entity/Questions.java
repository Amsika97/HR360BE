package com.maveric.hr360.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "questions")
public class Questions extends IdentifiedEntity {
    private String questionId;
    @DBRef
    QuestionSection questionSection;
    private String level;
    private String question;
    private int sequence;
    private String questionType;
    private String messageType;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;

    public Questions() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
