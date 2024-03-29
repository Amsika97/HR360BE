package com.maveric.hr360.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.data.mongodb.core.mapping.Document;


import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Document(collection = "surveydetails")
public class SurveyDetails extends IdentifiedEntity {


    private String surveyName;
    private String templateId;
    private String grade;

    private List<EmployeesResponse> employeesResponses;
    private String startDate;
    private String endDate;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;


}
