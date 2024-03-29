package com.maveric.hr360.clients.echart;

import com.maveric.hr360.clients.echart.response.EChartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.maveric.hr360.utils.Constants.NODE_CHART_SERVER;

@FeignClient(value = "chart-api", url = NODE_CHART_SERVER)
public interface ChartApiClient {

    @PostMapping("/")
    EChartResponse generateChart(@RequestBody Object request);
}
