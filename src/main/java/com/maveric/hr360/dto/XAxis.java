package com.maveric.hr360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XAxis {

    private int min;
    private int max;
    private AxisLine axisLine;
    private AxisLabel axisLabel;
}
