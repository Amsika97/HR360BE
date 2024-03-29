package com.maveric.hr360.clients.echart.response;

import lombok.Data;

@Data
public class EChartResponse {

    private int code;
    private String msg;
    private Object data;
}
