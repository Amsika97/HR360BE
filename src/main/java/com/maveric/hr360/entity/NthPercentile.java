package com.maveric.hr360.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "nthpercentile")
public class NthPercentile extends IdentifiedEntity{
    private String surveyName;
    private String grade;
    private int nthPercent;
    private Double customerManagement;
    private Double deliveryManagement;
    private Double leadershipSkills;
    private Double teamManagement;
    private Double grandTotal;
}