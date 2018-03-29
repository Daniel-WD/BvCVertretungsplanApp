package com.titaniel.bvcvertretungsplan.day_pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.titaniel.bvcvertretungsplan.DayManager;

public class DayPagerAdapter extends FragmentPagerAdapter {

    public DayPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        String number = String.valueOf(DayManager.dates[position].getDayOfMonth());
        String month = DayManager.monthList[position] + " " + DayManager.dates[position].getYear();
        return DayPagerFragment.newInstance(
                DayManager.capsDayList[position], month, number);
    }

    @Override
    public int getCount() {
        return DayManager.capsDayList.length;
    }

}
