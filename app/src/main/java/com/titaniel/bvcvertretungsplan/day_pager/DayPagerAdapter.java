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
        return DayPagerFragment.newInstance(DayManager.capsDayList[position]);
    }

    @Override
    public int getCount() {
        return DayManager.capsDayList.length;
    }

}
