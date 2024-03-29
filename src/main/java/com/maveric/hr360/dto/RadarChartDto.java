package com.maveric.hr360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadarChartDto {

    private int width;
    private int height;
    private String type;
    private boolean base64;
    private boolean download;
    private RadarOption option;
}
