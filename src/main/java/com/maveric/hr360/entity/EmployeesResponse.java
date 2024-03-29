package com.maveric.hr360.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class EmployeesResponse {
    private String employeeId;
    private List<ResponseList> responsesList;

}
