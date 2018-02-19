package com.titaniel.bvcvertretungsplan.database;

import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Calendar;

public class DataUtils {

    static int monthInName(String name) {
        return Integer.valueOf(name.substring(3, 5));
    }

    static int dayInName(String name) {
        return Integer.valueOf(name.substring(5, 7));
    }

    static int yearInName(String name) {
        return Integer.valueOf("20" + name.substring(1, 3));
    }

    static int dayInDate(String date) {
        return Integer.valueOf(date.substring(0, 2));
    }

    static int monthInDate(String date) {
        return Integer.valueOf(date.substring(3, 5));
    }

    static int yearInDate(String date) {
        return Integer.valueOf(date.substring(6, 10));
    }

    static int hoursInDate(String date) {
        return Integer.valueOf(date.substring(12, 14));
    }

    static int minutesInDate(String date) {
        return Integer.valueOf(date.substring(15, 17));
    }

    static LocalDateTime calendarToLocalDateTime(Calendar cal) {
        return new LocalDateTime(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH)+1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
    }

    static Database.Day findDay(FTPFile file) {
        for(Database.Day day : Database.days) {
            if(day.name.equals(file.getName())) return day;
        }
        return null;
    }

    static String[] toStringArray(ArrayList<FTPFile> files) {
        if(files == null) return null;
        String[] res = new String[files.size()];
        for(int i = 0; i < files.size(); i++) {
            res[i] = files.get(i).getName();
        }
        return res;
    }

}
