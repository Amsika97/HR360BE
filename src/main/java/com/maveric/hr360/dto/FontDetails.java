package com.maveric.hr360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FontDetails {

    private String fontStyle;
    private String fontWeight;
    private String fontFamily;
    private int fontSize;
    private String color;
}
