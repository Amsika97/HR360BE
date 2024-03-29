package com.maveric.hr360.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionsList {
    private String questionId;
    private String questionText;
    private Double collectiveOverallScore;
    private List<Response> responseList;

}