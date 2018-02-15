package com.titaniel.bvcvertretungsplan;

import android.content.Context;
import android.util.Log;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

public class DayManager {

    public static String[] capsDayList = new String[5];
    public static String[] shortDayList = new String[5];

    public static String[] dateList = new String[5];

    public static void init(Context context) {
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

        LocalDate[] order = new LocalDate[5];//five days
        int dayOrderIndex = 0;
        LocalDate localDate = LocalDate.now();
        for(int i = 0; i < 7; i++) {
            switch(localDate.getDayOfWeek()) {
                case DateTimeConstants.SATURDAY:
                case DateTimeConstants.SUNDAY:
                    localDate = localDate.plusDays(1);
                    continue;
            }
            order[dayOrderIndex++] = localDate;
            localDate = localDate.plusDays(1);
        }

        fillWeekDays(order, srcCapsDays, capsDayList);
        fillWeekDays(order, srcShortDays, shortDayList);

        for(int i = 0; i < order.length; i++) {
            dateList[i] = context.getString(R.string.temp_date,
                    order[i].getDayOfMonth(), srcMonths[order[i].getMonthOfYear()-1]);
        }

    }

    private static void fillWeekDays(LocalDate[] order, String[] src, String[] target) {
        for(int i = 0; i < order.length; i++) {
            target[i] = src[order[i].getDayOfWeek()-1];
        }
    }

}
