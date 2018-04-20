package com.titaniel.bvcvertretungsplan.main_activity.detail_fragment.header_pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.titaniel.bvcvertretungsplan.DateManager;

public class HeaderPagerAdapter extends FragmentPagerAdapter {

    public HeaderPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        String number = String.valueOf(DateManager.preparedDates[position].getDayOfMonth());
        String month = DateManager.preparedMonthList[position] + " " + DateManager.preparedDates[position].getYear();
        return HeaderPagerFragment.newInstance(
                DateManager.preparedCapsDayList[position], month, number);
    }

    @Override
    public int getCount() {
        return DateManager.preparedCapsDayList.length;
    }

}
