package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Daniel Weidensdörfer
 *
 * Enthält alle Daten für die Auslesung der XML Dateien (wie Tags) und speichert Objekte aller
 * Einträge der Vertretungspläne in geordneter Datenstruktur.
 */
public class Database {
    public static final String SERVER_LOCATION = "http://www.cottagym.selfhost.eu/images/cottaintern/vp/";

    // DIE FOLGENDEN KONSTANTEN SIND SCHLÜSSELWORTER IN DER VERTRETUNGSPLAN XML

    //Day
    static final String KEY_LAST_UPDATED = "datum";
    static final String KEY_DISABLED_COURSES = "abwesendk";
    static final String KEY_DISABLED_ROOMS = "abwesendr";

    //Entry
    static final String KEY_ENTRY = "aktion";
    static final String KEY_COURSE = "klasse";
    static final String KEY_HOURS = "stunde";
    static final String KEY_LESSON = "fach";
    static final String KEY_TEACHER = "lehrer";
    static final String KEY_ROOM = "raum";
    static final String KEY_INFO = "info";

    //Entry attrs
    static final String KEY_LESSON_CHANGE = "fageaendert";
    static final String KEY_TEACHER_CHANGE = "legeaendert";
    static final String KEY_ROOM_CHANGE = "rageaendert";
    static final String KEY_TRUE = "ae";

    /**
     * Repräsentiert einen Tag mit Vertretung
     */
    public static class Day {
        public String name; //Dateiname
        public ArrayList<Entry> entries = new ArrayList<>(); //Einträge
        public LocalDate date; //Datum
        public LocalDateTime lastUpdate;  //zuletzt aktuallisiert
        public String disabledClasses; //nicht anwesende Klassen
        public String disabledRooms; //nicht betretbare Räume :D
    }

    /**
     * Repräsentiert einen Eintrag im Vertretungsplan
     */
    public static class Entry implements Comparable<Entry> {
        public Course course; //Klasse (auch wenn da Course steht ist Klasse gemeint, da Class ein Java Schlüsselwort ist und deshalb nicht als Klassenname verwendet werden kann)
        public Hours hours; //betreffende Stunden
        public String courseString, hoursString, hoursText, lesson, teacher, room, info; //Rohe ausgelesene Daten aus dem XML
        public boolean lessonChange = false, teacherChange = false, roomChange = false; //was sich verändert hat
        public int specVisible = View.VISIBLE; //Layout bezogen... sagt ob das layout mit der sogenannten Specification also zum Bsp Pnw1, MA1 sichtbar ist

        @Override
        public int compareTo(@NonNull Entry o) {
            return hours.compareTo(o.hours);
        }

        Entry copy() {
            Entry entry = new Entry();
            entry.course = course;
            entry.hours = hours;
            entry.courseString = courseString;
            entry.hoursString = hoursString;
            entry.lesson = lesson;
            entry.teacher = teacher;
            entry.room = room;
            entry.info = info;
            entry.lessonChange = lessonChange;
            entry.teacherChange = teacherChange;
            entry.roomChange = roomChange;
            entry.specVisible = specVisible;
            entry.hoursText = hoursText;
            return entry;
        }

    }

    /**
     * Repräsentiert eine Klasse oder Klassenstufe, je nachdem was als <code>degree</code> eingetragen
     * ist. Dient der Zuordnung für den Kurs- und Klassenfilter
     *
     * Class als Name der Klasse geht nicht, da class ein Schlüsselwort in Java ist, also steht Course hier eigentlich für Klasse
     */
    public static class Course {
        public int degree = 0/*Klasse (5-12)*/, number = 0 /*für Klassen 5-10 als Anhängsel ... 5/1, 7/4*/;
        public String specification = ""; // Zum Beispiel MA1, de4, bwl2, Pspo2...

        Course copy() {
            Course res = new Course();
            res.degree = degree;
            res.number = number;
            res.specification = specification;
            return res;
        }

        @Override
        public String toString() {
            return "Course{" +
                    "degree=" + degree +
                    ", number=" + number +
                    ", specification='" + specification + '\'' +
                    '}';
        }
    }

    /**
     * Repräsentiert die Stunden für die jeweilige Vertretung
     */
    public static class Hours implements Comparable<Hours> {
        public int startHour = 0, endHour = 0; //selbsterklärend

        /**
         * Vergleich der Einträge aufsteigend nach Stunden
         * @param other stunde mit der Verglichen wird
         * @return negativ wenn diese Klasse unter <code>other</code> steht, positiv wenn darüber, 0 wenn gleich
         */
        @Override
        public int compareTo(@NonNull Hours other) {
            if((startHour == endHour && other.startHour == other.endHour)
                    || (startHour != endHour && other.startHour != other.endHour)) {
                return Integer.compare(startHour, other.startHour);
            } else if(startHour == endHour) {
                if(startHour <= other.endHour) {
                    return -1;
                } else {
                    return +1;
                }
            } else {
                if(other.startHour <= endHour) {
                    return +1;
                } else {
                    return -1;
                }
            }
        }

        @Override
        public String toString() {
            return "Hours{" +
                    "startHour=" + startHour +
                    ", endHour=" + endHour +
                    '}';
        }


    }

    public static final ArrayList<Day> days = new ArrayList<>();

    //SCHLÜSSEL WÖRTER FÜR DAS SPEICHERN DER DATEN AUF DEM GERÄT
    private static final String KEY_COURSE_DEGREE = "key_course_degree";
    private static final String KEY_COURSE_NUMBER = "key_course_number";
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_PASSWORD = "key_password";
    private static final String KEY_SELECTED_COURSES = "key_selected_courses";
    private static final String KEY_COURSE_CHOSEN = "key_course_chosen";

    public static String courseDegree = "5";
    public static String courseNumber = "1";
    public static String username = "";
    public static String password = "";
    public static boolean classChosen = false; //ob bereits ein Kurs gewählt wurde... relevant für den ersten Start und wenn die Login Daten verändert werden
    public static ArrayList<String> selectedCourses = new ArrayList<>(); //11/12 only  // Whitelist für Kursefilter für Klasse 11/12

    private static SharedPreferences sPrefs;

    public static boolean loaded = false; //ob alle Daten gedownloaded und gelesen wurden

    /**
     * Initialisierung
     * @param context Context
     */
    public static void init(Context context) {
        sPrefs = ((AppCompatActivity) context).getPreferences(Context.MODE_PRIVATE);
    }

    /**
     * Ruft den Thread auf, der die XML Daten runterläd und liest
     * @param context Context
     * @param offline ob man offline ist, wenn ja, dann wird nur gelesen(vorhandene Daten) und nichts heruntergeladen
     */
    public static void fetchData(Context context, boolean offline) {
        new LoadingTask().execute(new LoadingTask.Input(context, offline));
    }

    public static Entry[] findEntriesByCourseAndDate(LocalDate date, int degree, int number) {
        Day curDay = null;
        for(Day day : days) {
            if(day.date.isEqual(date)) curDay = day;
        }
        if(curDay == null) return null;
        ArrayList<Entry> res = new ArrayList<>();
        for(Entry entry : curDay.entries) {
                if(entry.course.degree == degree
                        && (degree > 10 || entry.course.number == number)
                        && (selectedCourses.isEmpty() || degree <= 10 || selectedCourses.contains(entry.course.specification))) {
                    res.add(entry);
                }
        }
        Entry[] output = new Entry[res.size()];
        return res.toArray(output);
    }

    /**
     * @see Database#findEntriesByCourseAndDate(LocalDate, int, int)
     * Aufruf der oben genannten Methode mit aktuell gewählter Klasse
     * @param date Datum
     * @return Array mit allen Einträgen für das Datum
     */
    public static Entry[] findEntriesByCourseAndDate(LocalDate date) {
        return findEntriesByCourseAndDate(date, Integer.parseInt(courseDegree), Integer.parseInt(courseNumber));
    }

    /**
     * Laden der lokal gespeicherten Daten, wie Klasse, Nutzername, Passwort und Whitelist der Kurse
     */
    public static void load() {
        courseDegree = sPrefs.getString(KEY_COURSE_DEGREE, courseDegree);
        courseNumber = sPrefs.getString(KEY_COURSE_NUMBER , courseNumber);
        username = sPrefs.getString(KEY_USERNAME, username);
        password = sPrefs.getString(KEY_PASSWORD, password);
        classChosen = sPrefs.getBoolean(KEY_COURSE_CHOSEN, classChosen);

        HashSet<String> courseSet = (HashSet<String>) sPrefs.getStringSet(KEY_SELECTED_COURSES, new HashSet<>());
        selectedCourses.addAll(Arrays.asList(courseSet.toArray(new String[courseSet.size()])));

//        username = "";
//        password = "";
//        courseDegree = "5";
//        courseNumber = "1";
//        classChosen = false;
    }

    /**
     * Speichern der lokal gespeicherten Daten, wie Klasse, Nutzername, Passwort und Whitelist der Kurse
     */
    public static void save() {
        HashSet<String> courseSet = new HashSet<>(selectedCourses);
        sPrefs.edit()
                .putBoolean(KEY_COURSE_CHOSEN, classChosen)
                .putStringSet(KEY_SELECTED_COURSES, courseSet)
                .putString(KEY_COURSE_DEGREE, courseDegree)
                .putString(KEY_COURSE_NUMBER, courseNumber)
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password).apply();
    }

}
