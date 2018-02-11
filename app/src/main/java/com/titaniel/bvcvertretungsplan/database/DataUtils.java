package com.titaniel.bvcvertretungsplan.database;

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



}
