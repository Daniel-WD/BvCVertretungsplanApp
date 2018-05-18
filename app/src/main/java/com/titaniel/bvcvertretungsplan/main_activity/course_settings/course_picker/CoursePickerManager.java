package com.titaniel.bvcvertretungsplan.main_activity.course_settings.course_picker;

import java.util.ArrayList;

public class CoursePickerManager {

    static class Course {
        String name, shortName;

        Course(String name, String shortName) {
            this.name = name;
            this.shortName = shortName;
        }
    }

    private final Course[] sSelectableCourses = {
            new Course("Mathe", "ma"),
            new Course("Deutsch", "de"),
            new Course("Biologie", "bio"),
            new Course("Physik", "ph"),
            new Course("Englisch", "en"),
            new Course("Geschichte", "ge"),
            new Course("Französisch", "fr"),
            new Course("Latein", "lat"),
            new Course("Russisch", "ru"),
            new Course("Astro", "ast"),
            new Course("Chemie", "ch"),
            new Course("Geographie", "geo"),
            new Course("Informatik", "inf"),
            new Course("Ethik", "eth"),
            new Course("GRW", "grw"),
            new Course("Kunst", "ku"),
            new Course("Musik", "mu"),
            new Course("BWL", "bwl")
    };


    private int mIndex = 0;

    int currentIndex() {
        return  mIndex;
    }

    Course currentCourse() {
        return sSelectableCourses[mIndex];
    }

    Course nextCourse() {
        if(++mIndex >= sSelectableCourses.length) return null;
        return sSelectableCourses[mIndex];
    }

    Course previousCourse() {
        if(--mIndex < 0) return null;
        return sSelectableCourses[mIndex];
    }

    boolean hasNext() {
        return mIndex +1 < sSelectableCourses.length;
    }

    boolean hasPrevious() {
        return mIndex -1 >= 0;
    }
}
