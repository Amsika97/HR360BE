package com.maveric.hr360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Series {
    private String type;
    private String barWidth;
    private String barGraph;
    private List<DataItem> data;
    private Label label;
}
