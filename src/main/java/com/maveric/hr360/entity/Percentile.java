package com.maveric.hr360.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Percentile extends IdentifiedEntity{
    private String surveyName;
    private String grade;
    private String employeeId;
    private Double customerManagement;
    private Double deliveryManagement;
    private Double leadershipSkills;
    private Double teamManagement;
    private Double grandTotal;
}
