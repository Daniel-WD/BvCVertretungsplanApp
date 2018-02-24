package com.titaniel.bvcvertretungsplan.main_pager;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;

public class MainPagerAdapter extends FragmentPagerAdapter {

    private EntryListFragment[] fragments = new EntryListFragment[5];
    private Handler handler = new Handler();

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public EntryListFragment getFragment(int pos) {
        return fragments[pos];
    }

    public RecyclerView getEntryList(int pos) {
        return fragments[pos].entryList;
    }

    public void update() {
        for(EntryListFragment fragment : fragments) {
            fragment.update();
        }
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        for(EntryListFragment fragment : fragments) {
            fragment.scrollEnabled = scrollEnabled;
        }
    }

    public void scrollToTop() {
        for(EntryListFragment fragment : fragments) {
            fragment.entryList.scrollToPosition(0);
        }
    }

    @Override
    public Fragment getItem(int position) {
        fragments[position] = EntryListFragment.newInstance(position);
        return fragments[position];
    }

    @Override
    public int getCount() {
        return 5;
    }

}
