package com.maveric.hr360.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Legend {

    private List<String> data;
    private String orient;
    private int bottom;
    private int right;
    private String left;
    private String top;
}
