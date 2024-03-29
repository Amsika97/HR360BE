package com.maveric.hr360.entity;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Data
@Document(collection = "absoluteScore")
@RequiredArgsConstructor
public class AbsoluteScore extends IdentifiedEntity {
    private String surveyName;
    private String employeeId;
    private String grade;
    private double customerManagement;
    private double deliveryManagement;
    private double leadershipSkills;
    private double teamManagement;
    private double grandTotal;
}
