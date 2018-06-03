package com.titaniel.bvcvertretungsplan.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.util.ArrayList;

/**
 * @author Daniel Weidensdörfer
 *
 * Klasse, um die Reihenfolge von Daten(Mehrzahl von Datum) und anderen Dingen wie Wochentagen zu bestimmen
 *
 * Bestimmt auch welche Dateien vom Server erfragt werden sollen
 *
 */
public class DateManager {

    private static String[] capsDayList = new String[5]; //Die nächsten 5 Werktage in Stringform
    private static LocalDate[] dates = new LocalDate[5]; //Die Daten(Mehrzahl von Datum) der nächsten 5 Tage

    public static String[] preparedCapsDayList; //das selbe wie <code>capsDayList</code> nur ohne die Tage, bei denen keine Vertretung vorhanden ist... Klassenspezifisch
    public static LocalDate[] preparedDates; //das selbe wie <code>dates</code> nur ohne die Tage, bei denen keine Vertretung vorhanden ist... Klassenspezifisch

    public static String[] serverFileList = new String[5]; //Dateinamen für die nächsten 5 Tage

    /**
     * Initialisierungen... Befüllen der Listen
     * @param context Context
     */
    public static void init(Context context) {

        String[] srcCapsDays = {
                context.getString(R.string.monday_caps),
                context.getString(R.string.tuesday_caps),
                context.getString(R.string.wednesday_caps),
                context.getString(R.string.thursday_caps),
                context.getString(R.string.friday_caps)
        };

        int dayOrderIndex = 0;
        LocalDate localDate = LocalDate.now();
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

        for(int i = 0; i < dates.length; i++) {
            capsDayList[i] = srcCapsDays[dates[i].getDayOfWeek()-1];
        }

    }

    /**
     * Befüllt die "prepared" Listen mit den Tagen, an denen Tatsächlich Vertretung vorhanden ist
     *
     * Man beachte, dass diese Methode immer aufgerufen wird, wenn eine Klasse gesetzt wird...
     * Denn die Vertretungstage sind ja verschieden...
     *
     */
    public static void prepare() {
        ArrayList<String> pCapsDayList = new ArrayList<>();
        ArrayList<LocalDate> pDates = new ArrayList<>();

        for(int i = 0; i < dates.length; i++) {
            Database.Entry[] entries = Database.findEntriesByCourseAndDate(
                    dates[i], Integer.parseInt(Database.courseDegree), Integer.parseInt(Database.courseNumber));
            if(entries != null && entries.length != 0) {
                pCapsDayList.add(capsDayList[i]);
                pDates.add(dates[i]);
            }
        }

        preparedCapsDayList = new String[pCapsDayList.size()];
        preparedDates = new LocalDate[pDates.size()];

        preparedCapsDayList = pCapsDayList.toArray(preparedCapsDayList);
        preparedDates = pDates.toArray(preparedDates);
    }
}
