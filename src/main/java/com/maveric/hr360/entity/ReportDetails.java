package com.maveric.hr360.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reportdetails")
public class ReportDetails extends IdentifiedEntity {
    private String surveyId;
    private String surveyName;
    private Long employeeId;
    private Boolean isProcessed;
    private List<QuestionsList> questionsList;
}

