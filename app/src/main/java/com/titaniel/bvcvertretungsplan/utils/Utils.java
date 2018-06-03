package com.titaniel.bvcvertretungsplan.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Daniel Weidensdörfer
 * Enthält nützliche Funktionen
 */
public class Utils {

    /**
     * Diese Funktion macht aus einer Zahl einen String mit einer bestimmten Anzahl an Nullen
     *
     * Wenn <code>number</code> = 4 und <code>length</code> = 2 dann kommt "04" raus...
     *
     * @param number Zahl
     * @param length Minimale Länge
     * @return Ausgabestring... Nummer mit Nullen davor(wenn möglich)
     */
    public static String intToStrWithLength(int number, int length) {
        StringBuilder res = new StringBuilder(String.valueOf(number));
        if(res.length() >= length) return res.toString();
        while(res.length() < length) {
            res.insert(0, "0");
        }
        return res.toString();
    }

    /**
     * Prüft ob eine Verbindung zum Internet besteht
     * @param context Context
     * @return true wenn online, false andernfalls
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

}
