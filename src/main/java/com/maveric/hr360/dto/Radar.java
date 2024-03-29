package com.maveric.hr360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Radar {

    private String shape;
    private List<Indicator> indicator;
    private AxisLine axisLine;
    private SplitLine splitLine;
    private List<String> center;
}
