package com.maveric.hr360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionDto {
    private String questionId;
    private String level;
    private String questionType;
    private String question;
    private Long questionSectionId;
    private int sequence;
    private String messageType;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
    public QuestionDto() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
