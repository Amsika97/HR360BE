package com.maveric.hr360.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YAxis {

    private List<String> data;
    private AxisLabel axisLabel;
}
