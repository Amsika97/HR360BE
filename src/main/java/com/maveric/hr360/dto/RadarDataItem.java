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
public class RadarDataItem {

    private List<Double> value;
    private ItemStyle itemStyle;
    private String name;
    private Label label;
    private LineStyle lineStyle;
}
