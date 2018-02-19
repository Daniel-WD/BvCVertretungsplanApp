package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;

public class Database {

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

    public static class Day {
        public String name;
        public ArrayList<Entry> entries = new ArrayList<>();
        public LocalDate date;
        public LocalDateTime lastUpdate;
        public String disabledCourses;
        public String disabledRooms;
    }

    public static class Entry {
        public String course, hours, lesson, teacher, room, info;
        public boolean lessonChange = false, teacherChange = false, roomChange = false;
    }

    public static final ArrayList<Day> days = new ArrayList<>();

    private static final String KEY_COURSE_DEGREE = "key_course_degree";
    private static final String KEY_COURSE_NUMBER = "key_course_number";

    public static String courseDegree = "5";
    public static String courseNumber = "1";

    private static SharedPreferences sPrefs;

    public static void init(Context context) {
        sPrefs = ((AppCompatActivity) context).getPreferences(Context.MODE_PRIVATE);
    }

    public static void fetchData(Context context) {

        new LoadingTask().execute(context);

    }

    public static void load() {
        courseDegree = sPrefs.getString(KEY_COURSE_DEGREE, "5");
        courseNumber = sPrefs.getString(KEY_COURSE_NUMBER , "1");
    }

    public static void save() {
        sPrefs.edit().putString(KEY_COURSE_DEGREE, courseDegree).putString(KEY_COURSE_NUMBER, courseNumber).apply();
    }

}
