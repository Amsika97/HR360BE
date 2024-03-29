package com.maveric.hr360.constant;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class ScoreCalculationConstants {

    public static final double ZERO=0.0;
    public static final String SCORE= "Score";
    public static final String SELF= "Self";
    public static final String MANAGER= "Manager";
    public static final String REPORTEE= "Reportee";
    public static final String PEER= "Peer";
    public static final String L0= "L0";
    public static final String T0= "T0";
    public static final String C0= "C0";
    public static final String D0= "D0";
    public static final String EMPLOYEEID= "employeeId";
    public static final String CUSTOMERMANAGEMENT= "customerManagement";
    public static final String DELIVERYMANAGEMENT= "deliveryManagement";
    public static final String LEADERSHIPSKILLS= "leadershipSkills";
    public static final String TEAMMANAGEMENT= "teamManagement";
    public static final String GRANDTOTAL= "grandTotal";
    public static final String SKIP_LEVEL_MANAGER= "Skip Level Manager";
    public static final String DIRECT_MANAGER= "Direct Manager";
    public static final String DIRECT_REPORTEE= "Direct Reportee";
    public static final String SKIP_LEVEL_REPOTEE= "Skip Level Reportee";
    public static final String L= "L";
    public static final String C= "C";
    public static final String T= "T";
    public static final String D= "D";
    public static final String ERROR_MESSAGE="Score calculation failed.Error while processing for EmployeeId - ";
    public static final String NthPercentile_ERROR_MESSAGE="Score calculation failed";

    /*public List<String> getPercentileValues() {
        String valuesString = env.getProperty("nthPercentile.values");
        List<String> valuesList = Arrays.asList(valuesString.split(","));
        return valuesList;
    }*/
}
