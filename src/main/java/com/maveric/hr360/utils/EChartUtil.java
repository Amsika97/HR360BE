package com.maveric.hr360.utils;

import com.maveric.hr360.dto.*;
import com.maveric.hr360.entity.AbsoluteScore;
import com.maveric.hr360.entity.NthPercentile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.maveric.hr360.utils.Constants.*;

@Slf4j
public class EChartUtil {

    public static EChartDto configBarChartDetails(List<String> data, String text){
        AxisLine axisLine = AxisLine.builder().show(true).build();
        AxisLabel axisLabel = new AxisLabel();
        axisLabel.setFontWeight("lighter");
        axisLabel.setFontFamily("arial");
        axisLabel.setFontSize(40);
        XAxis xAxis = XAxis.builder()
                .axisLine(axisLine)
                .axisLabel(axisLabel)
                .max(10)
                .min(0)
                .build();
        YAxis yAxis = YAxis.builder().data(data).axisLabel(axisLabel).build();
        Title title = Title.builder()
                .text(text)
                .left("50%")
                .textAlign("center")
                .textStyle(new TextStyle())
                .build();
        title.getTextStyle().setFontSize(40);

        Label label = Label.builder()
                .show(true)
                .position("right")
                .color("#000000")
                .build();
        label.setFontStyle("lighter");
        label.setFontSize(30);
        Series series = Series.builder()
                .type("bar")
                .barWidth("50%")
                .barGraph("50%")
                .label(label)
                .build();
        List<Series> seriesList = new ArrayList<>();
        seriesList.add(series);
        Option option = Option.builder()
                .series(seriesList)
                .title(title)
                .yAxis(yAxis)
                .xAxis(xAxis)
                .backgroundColor("#ffffff")
                .build();

        return EChartDto.builder()
                .type("png")
                .width(1950)
                .height(733)
                .base64(true)
                .download(false)
                .option(option)
                .build();
    }

    public static Map<String,String> fetchDataItem(){
        Map<String,String> dataItemMap = new HashMap<>();
        dataItemMap.put(MANAGER,"#8064A2");
        dataItemMap.put(PEER,"#9BBB59");
        dataItemMap.put(REPORTEE,"#C0504D");
        dataItemMap.put(SELF,"#4F81BD");
        dataItemMap.put(COLLECTIVE,"#F79646");
        return dataItemMap;
    }

    public static RadarChartDto configureRadarChartDetails(List<String> legendList,
                                                           List<RadarDataItem> radarDataItems){

        List<RadarSeries> radarSeriesList = new ArrayList<>();
        radarSeriesList.add(RadarSeries.builder()
                .type("radar")
                .data(radarDataItems)
                .build());

        List<String> center = new ArrayList<>();
        center.add("30%");
        center.add("50%");
        Radar radar = Radar.builder()
                .indicator(getIndicators())
                .shape("polygon")
                .axisLine(AxisLine.builder().show(false).build())
                .splitLine(SplitLine.builder().show(false).build())
                .center(center)
                .build();


        TextStyle textStyle = new TextStyle();
        textStyle.setFontSize(40);
        textStyle.setFontWeight("normal");
        textStyle.setFontFamily("arial");
        textStyle.setColor("#00008b");

        RadarOption option = RadarOption.builder()
                .legend(Legend.builder()
                        .data(legendList)
                        .orient("vertical")
                        .left("50%")
                        .top("center")
                        .build())
                .radar(radar)
                .series(radarSeriesList)
                .textStyle(textStyle)
                .build();
        return RadarChartDto.builder()
                .type("png")
                .width(3000)
                .height(1125)
                .base64(true)
                .download(false)
                .option(option)
                .build();
    }

    private static List<Indicator> getIndicators() {
        List<Indicator> indicator = new ArrayList<>();
        indicator.add(Indicator.builder().name("Overall").max(10).build());
        indicator.add(Indicator.builder().name("Customer Management").max(10).build());
        indicator.add(Indicator.builder().name("Leadership Skills").max(10).build());
        indicator.add(Indicator.builder().name("Team Management").max(10).build());
        indicator.add(Indicator.builder().name("Delivery Management").max(10).build());
        return indicator;
    }

    public static void deleteFilesInFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
                log.info("All files inside the folder have been deleted successfully.");
            } else {
                log.info("No files found inside the folder.");
            }
        } else {
            log.error("Folder does not exist or is not a directory.");
        }
    }

}
