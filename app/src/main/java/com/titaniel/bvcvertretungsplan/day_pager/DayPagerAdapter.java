package com.titaniel.bvcvertretungsplan.day_pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DayPagerAdapter extends FragmentPagerAdapter {

    public DayPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    private String[] texts = {
            "MONTAG",
            "DIENSTAG",
            "MITTWOCH",
            "DONNERSTAG",
            "FREITAG"
    };

    @Override
    public Fragment getItem(int position) {
        return DayPagerFragment.newInstance(texts[position]);
    }

    @Override
    public int getCount() {
        return texts.length;
    }

}
