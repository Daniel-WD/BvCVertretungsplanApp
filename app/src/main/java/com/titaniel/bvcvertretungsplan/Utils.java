package com.titaniel.bvcvertretungsplan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {

    public static String intToStrWithLength(int number, int length) {
        StringBuilder res = new StringBuilder(String.valueOf(number));
        if(res.length() >= length) return res.toString();
        while(res.length() < length) {
            res.insert(0, "0");
        }
        return res.toString();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

}
