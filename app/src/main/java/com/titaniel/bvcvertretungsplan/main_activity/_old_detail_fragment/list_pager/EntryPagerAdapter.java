package com.titaniel.bvcvertretungsplan.main_activity._old_detail_fragment.list_pager;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;

import com.titaniel.bvcvertretungsplan.DateManager;

public class EntryPagerAdapter extends FragmentPagerAdapter {

    private EntryPagerFragment[] fragments = new EntryPagerFragment[DateManager.preparedDates.length];
    private Handler handler = new Handler();

    public EntryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public EntryPagerFragment getFragment(int pos) {
        return fragments[pos];
    }

    public RecyclerView getEntryList(int pos) {
        return fragments[pos].entryList;
    }

    public void update() {
        for(EntryPagerFragment fragment : fragments) {
            fragment.update();
        }
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        for(EntryPagerFragment fragment : fragments) {
            fragment.scrollEnabled = scrollEnabled;
        }
    }

    public void scrollToTop() {
        for(EntryPagerFragment fragment : fragments) {
            fragment.entryList.scrollToPosition(0);
        }
    }

    @Override
    public Fragment getItem(int position) {
        fragments[position] = EntryPagerFragment.newInstance(position);
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

}
