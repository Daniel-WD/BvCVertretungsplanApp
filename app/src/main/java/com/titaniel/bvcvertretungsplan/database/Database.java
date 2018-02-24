package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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

    public static class Entry implements Comparable<Entry> {
        public Course course;
        public Hours hours;
        public String courseString, hoursString, lesson, teacher, room, info;
        public boolean lessonChange = false, teacherChange = false, roomChange = false;

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
            return entry;
        }

    }

    public static class Course {
        public int degree = 0, number = 0;
        public String specification = "";

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

    public static class Hours implements Comparable<Hours> {
        public int startHour = 0, endHour = 0;

        @Override
        public int compareTo(@NonNull Hours o) {
            if((startHour == endHour && o.startHour == o.endHour) || (startHour != endHour && o.startHour != o.endHour)) {
                return Integer.compare(startHour, o.startHour);
            } else if(startHour == endHour) {
                if(startHour <= o.endHour) {
                    return -1;
                } else {
                    return +1;
                }
            } else {
                if(o.startHour <= endHour) {
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

    private static final String KEY_COURSE_DEGREE = "key_course_degree";
    private static final String KEY_COURSE_NUMBER = "key_course_number";

    public static String courseDegree = "5";
    public static String courseNumber = "1";

    private static SharedPreferences sPrefs;

    public static boolean loaded = false;

    public static void init(Context context) {
        sPrefs = ((AppCompatActivity) context).getPreferences(Context.MODE_PRIVATE);
    }

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
            if(entry.course.degree == degree && (degree > 10 || entry.course.number == number)) {
                res.add(entry);
            }
        }
        Entry[] output = new Entry[res.size()];
        return res.toArray(output);
    }

    public static void load() {
        courseDegree = sPrefs.getString(KEY_COURSE_DEGREE, "5");
        courseNumber = sPrefs.getString(KEY_COURSE_NUMBER , "1");
    }

    public static void save() {
        sPrefs.edit().putString(KEY_COURSE_DEGREE, courseDegree).putString(KEY_COURSE_NUMBER, courseNumber).apply();
    }

}
