package com.titaniel.bvcvertretungsplan.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.util.ArrayList;

public class DateManager {

    public static String[] preparedCapsDayList;
    public static String[] preparedShortDayList;
    public static String[] preparedMonthList;
    public static LocalDate[] preparedDates;
    public static Integer[] preparedColors;

    private static String[] capsDayList = new String[5];
    public static String[] shortDayList = new String[5];
    private static String[] monthList = new String[5];
    private static LocalDate[] dates = new LocalDate[5];
    private static int[] colors = new int[5];

    public static String[] serverFileList = new String[5];

    public static void init(Context context) {

        int[] srcColors =  {
                ContextCompat.getColor(context, R.color.monday),
                ContextCompat.getColor(context, R.color.tuesday),
                ContextCompat.getColor(context, R.color.wednesday),
                ContextCompat.getColor(context, R.color.thursday),
                ContextCompat.getColor(context, R.color.friday)
        };
        String[] srcCapsDays = {
                context.getString(R.string.monday_caps),
                context.getString(R.string.tuesday_caps),
                context.getString(R.string.wednesday_caps),
                context.getString(R.string.thursday_caps),
                context.getString(R.string.friday_caps)
        };
        String[] srcShortDays = {
                context.getString(R.string.monday_short),
                context.getString(R.string.tuesday_short),
                context.getString(R.string.wednesday_short),
                context.getString(R.string.thursday_short),
                context.getString(R.string.friday_short)
        };
        String[] srcMonths = {
                context.getString(R.string.january),
                context.getString(R.string.february),
                context.getString(R.string.march),
                context.getString(R.string.april),
                context.getString(R.string.may),
                context.getString(R.string.june),
                context.getString(R.string.juli),
                context.getString(R.string.august),
                context.getString(R.string.september),
                context.getString(R.string.october),
                context.getString(R.string.november),
                context.getString(R.string.december)
        };

        int dayOrderIndex = 0;
        LocalDate localDate = LocalDate.now();//todo oisdföoadhjgfalkvjaonfoiv n jf pojfpv if wvof ö
        for(int i = 0; i < 7; i++) {
            switch(localDate.getDayOfWeek()) {
                case DateTimeConstants.SATURDAY:
                case DateTimeConstants.SUNDAY:
                    localDate = localDate.plusDays(1);
                    continue;
            }
            dates[dayOrderIndex] = localDate;
            String fileString = "k" + Utils.intToStrWithLength(localDate.getYearOfCentury(), 2)
                    + Utils.intToStrWithLength(localDate.getMonthOfYear(), 2)
                    + Utils.intToStrWithLength(localDate.getDayOfMonth(), 2) + ".xml";
            serverFileList[dayOrderIndex] = fileString;
            dayOrderIndex++;
            localDate = localDate.plusDays(1);
        }

        fillWeekDays(dates, srcCapsDays, capsDayList);
        fillWeekDays(dates, srcShortDays, shortDayList);
        fillColors(dates, srcColors, colors);

        for(int i = 0; i < dates.length; i++) {
            monthList[i] = srcMonths[dates[i].getMonthOfYear()-1];
        }

    }

    public static void prepare() {
        ArrayList<String> pCapsDayList = new ArrayList<>();
        ArrayList<String> pShortDayList = new ArrayList<>();
        ArrayList<String> pMonthList = new ArrayList<>();
        ArrayList<LocalDate> pDates = new ArrayList<>();
        ArrayList<Integer> pColors = new ArrayList<>();

        for(int i = 0; i < dates.length; i++) {
            Database.Entry[] entries = Database.findEntriesByCourseAndDate(
                    dates[i], Integer.parseInt(Database.courseDegree), Integer.parseInt(Database.courseNumber));
            if(entries != null && entries.length != 0) {
                pCapsDayList.add(capsDayList[i]);
                pShortDayList.add(shortDayList[i]);
                pMonthList.add(monthList[i]);
                pDates.add(dates[i]);
                pColors.add(colors[i]);
            }
        }

        preparedCapsDayList = new String[pCapsDayList.size()];
        preparedShortDayList = new String[pShortDayList.size()];
        preparedMonthList = new String[pMonthList.size()];
        preparedDates = new LocalDate[pDates.size()];
        preparedColors = new Integer[pColors.size()];

        preparedCapsDayList = pCapsDayList.toArray(preparedCapsDayList);
        preparedShortDayList = pShortDayList.toArray(preparedShortDayList);
        preparedMonthList = pMonthList.toArray(preparedMonthList);
        preparedDates = pDates.toArray(preparedDates);
        preparedColors = pColors.toArray(preparedColors);
    }

    private static void fillWeekDays(LocalDate[] order, String[] src, String[] target) {
        for(int i = 0; i < order.length; i++) {
            target[i] = src[order[i].getDayOfWeek()-1];
        }
    }

    private static void fillColors(LocalDate[] order, int[] src, int[] target) {
        for(int i = 0; i < order.length; i++) {
            target[i] = src[order[i].getDayOfWeek()-1];
        }
    }



}
