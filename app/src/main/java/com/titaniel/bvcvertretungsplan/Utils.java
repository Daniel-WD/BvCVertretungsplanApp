package com.titaniel.bvcvertretungsplan;

public class Utils {

    public static String intToStrWithLength(int number, int length) {
        StringBuilder res = new StringBuilder(String.valueOf(number));
        if(res.length() >= length) return res.toString();
        while(res.length() < length) {
            res.insert(0, "0");
        }
        return res.toString();
    }

}
