package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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

    static class Day {
        ArrayList<Entry> entries = new ArrayList<>();
        Calendar date, lastUpdate;
        String disabledCourses;
        String disabledRooms;
    }

    static class Entry {
        String course, hours, lesson, teacher, room, info;
        boolean lessonChange = false, teacherChange = false, roomChange = false;
    }

    static final ArrayList<String> savedFiles = new ArrayList<>();

    static final ArrayList<Day> day = new ArrayList<>();

    public static void init(Context context) {

        savedFiles.addAll(Arrays.asList(context.fileList()));

        new LoadingTask().execute(context);

    }

}
