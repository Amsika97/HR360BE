package com.maveric.hr360.entity;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Data
@Document(collection = "employee")
@RequiredArgsConstructor
public class Employee extends IdentifiedEntity{

    private String employeeId;
    private String employeeFullName;
    private String businessPhoneNumber;
    private String mavericMail;
    private String deliveryUnit;
    private String account;
    private String createdAt;
    private String  updatedAt;
    private String  createdBy;
    private String  updatedBy;

}
