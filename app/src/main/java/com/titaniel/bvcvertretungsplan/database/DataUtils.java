package com.titaniel.bvcvertretungsplan.database;

import android.provider.ContactsContract;

import org.joda.time.LocalDateTime;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

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

    static LocalDateTime calendarToLocalDateTime(Calendar cal) {
        return new LocalDateTime(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH)+1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
    }

    static Database.Day findDay(File file) {
        for(Database.Day day : Database.days) {
            if(day.name.equals(file.getName())) return day;
        }
        return null;
    }

    static Database.Course[] findCourses(String courseString) {
        ArrayList<Database.Course> result = new ArrayList<>();
        ArrayList<String> blocks = new ArrayList<>();
        String specification = "";
        StringBuilder builder = new StringBuilder(courseString);

        if(builder.toString().contains(",")) {
            while(builder.toString().contains(",")) {
                int index = builder.indexOf(",");
                blocks.add(builder.substring(0, index));
                builder.replace(0, index+1, "");
                if(builder.toString().contains(",")) {
                    continue;
                } else if(builder.toString().contains("/")) {
                    int dex = builder.indexOf("/");
                    blocks.add(builder.substring(0, dex));
                    break;
                } else {
                    blocks.add(builder.substring(0, builder.length()));
                    break;
                }
            }
        } else {
            //index to end or to '/'
            int dex = builder.indexOf("/");
            dex = dex == -1 ? builder.length() : dex;
            blocks.add(builder.substring(0, dex));
//            if(builder.toString().contains("-")) {
//                blocks.add(builder.substring(0, 9));
//            } else {
//                blocks.add(builder.substring(0, 4));
//            }
        }

        //specification
        if(courseString.contains("/")) {
            specification = courseString.substring(courseString.indexOf("/")+1).trim();
        }

        for(String block : blocks) {
            if(block.contains("-")) {
                int index = block.indexOf("-");
                Database.Course startCourse = resolveBlock(block.substring(0, index), specification);
                Database.Course endCourse = resolveBlock(block.substring(index+1), specification);
                result.add(startCourse);
                result.add(endCourse);
                if(startCourse.degree == endCourse.degree && startCourse.number != 0 && endCourse.number != 0) {
                    for(int i = startCourse.number+1; i < endCourse.number; i++) {
                        Database.Course newCourse = startCourse.copy();
                        newCourse.number = i;
                        result.add(newCourse);
                    }
                }
            } else {
                Database.Course course = resolveBlock(block, specification);
                result.add(course);
            }
        }

        Database.Course[] res = new Database.Course[result.size()];
        return result.toArray(res);
    }

    private static Database.Course resolveBlock(String block, String spec) {
        Database.Course course = new Database.Course();
        course.degree = Integer.parseInt(block.substring(0, 2));

        if(course.degree != 11 && course.degree != 12) {
            course.number = Integer.parseInt(block.substring(3, 4));
        }
        course.specification = spec;
        return course;
    }

    static Database.Hours findHours(String hoursString) {
        Database.Hours res = new Database.Hours();
        for(int i = 0; i < hoursString.length(); i++) {
            char c = hoursString.charAt(i);
            if(Character.isDigit(c)) {
                if(res.startHour == 0) res.startHour = Integer.parseInt(String.valueOf(c));
                else if(res.endHour == 0) {
                    res.endHour = Integer.parseInt(String.valueOf(c));
                    break;
                }
            }
        }
        if(res.endHour == 0) res.endHour = res.startHour;
        return res;
    }

}
