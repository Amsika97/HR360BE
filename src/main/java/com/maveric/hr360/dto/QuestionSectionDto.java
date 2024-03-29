package com.maveric.hr360.dto;


import com.maveric.hr360.entity.Weightages;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class QuestionSectionDto {
    private String sectionName;
    private List<Weightages> weightages;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
    public QuestionSectionDto() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
