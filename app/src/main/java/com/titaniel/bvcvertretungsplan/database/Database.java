package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Database {


    public static final String SERVER_LOCATION = "http://www.cottagym.selfhost.eu/images/cottaintern/vp/";

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
        public String hoursText;
        public boolean lessonChange = false, teacherChange = false, roomChange = false;
        public int lessonChangeVisible = View.VISIBLE;
        public int teacherChangeVisible = View.VISIBLE;
        public int roomChangeVisible = View.VISIBLE;
        public int specVisible = View.VISIBLE;
        public int breakOutVisible = View.VISIBLE;

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
            entry.teacherChangeVisible = teacherChangeVisible;
            entry.roomChangeVisible = roomChangeVisible;
            entry.lessonChangeVisible = lessonChangeVisible;
            entry.specVisible = specVisible;
            entry.hoursText = hoursText;
            entry.breakOutVisible = breakOutVisible;
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
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_PASSWORD = "key_password";
    private static final String KEY_SELECTED_COURSES = "key_selected_courses";
    private static final String KEY_COURSE_CHOSEN = "key_course_chosen";

    public static String courseDegree = "5";
    public static String courseNumber = "1";
    public static String username = "";
    public static String password = "";
    public static boolean courseChosen = false;
    public static ArrayList<String> selectedCourses = new ArrayList<>(); //11/12 only

    private static SharedPreferences sPrefs;

    public static boolean loaded = false;

    public static void init(Context context) {
        sPrefs = ((AppCompatActivity) context).getPreferences(Context.MODE_PRIVATE);
    }

    public static void fetchData(Context context, boolean offline) {
        new LoadingTask().execute(new LoadingTask.Input(context, offline));
    }

    public static Entry[] findEntriesByCourseAndDate(LocalDate date) {
        return findEntriesByCourseAndDate(date, Integer.parseInt(courseDegree), Integer.parseInt(courseNumber));
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

    public static void load() {
        courseDegree = sPrefs.getString(KEY_COURSE_DEGREE, courseDegree);
        courseNumber = sPrefs.getString(KEY_COURSE_NUMBER , courseNumber);
        username = sPrefs.getString(KEY_USERNAME, username);
        password = sPrefs.getString(KEY_PASSWORD, password);
        courseChosen = sPrefs.getBoolean(KEY_COURSE_CHOSEN, courseChosen);

        HashSet<String> courseSet = (HashSet<String>) sPrefs.getStringSet(KEY_SELECTED_COURSES, new HashSet<>());
        selectedCourses.addAll(Arrays.asList(courseSet.toArray(new String[courseSet.size()])));

//        username = "";
//        password = "";
//        courseDegree = "5";
//        courseNumber = "1";
//        courseChosen = false;
    }

    public static void save() {
        HashSet<String> courseSet = new HashSet<>(selectedCourses);
        sPrefs.edit()
                .putBoolean(KEY_COURSE_CHOSEN, courseChosen)
                .putStringSet(KEY_SELECTED_COURSES, courseSet)
                .putString(KEY_COURSE_DEGREE, courseDegree)
                .putString(KEY_COURSE_NUMBER, courseNumber)
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password).apply();
    }

}
