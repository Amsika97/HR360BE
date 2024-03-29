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
public class RadarOption {

    private Legend legend;
    private Radar radar;
    private List<RadarSeries> series;
    private TextStyle textStyle;
}
