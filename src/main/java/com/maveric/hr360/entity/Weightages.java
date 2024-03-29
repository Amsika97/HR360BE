package com.maveric.hr360.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Weightages {
    private String respondentCategory;
    private Double weightage;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;

    public Weightages() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
