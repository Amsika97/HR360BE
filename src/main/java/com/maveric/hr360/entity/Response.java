package com.maveric.hr360.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response {
    private String respondentCategory;
    private String responseType;
    private String responseScore;
    private String responseMessage;
}
