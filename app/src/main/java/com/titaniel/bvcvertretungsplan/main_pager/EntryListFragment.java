package com.titaniel.bvcvertretungsplan.main_pager;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DayManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.main_pager.entry_list.EntryItemDecoration;
import com.titaniel.bvcvertretungsplan.main_pager.entry_list.EntryListAdapter;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class EntryListFragment extends Fragment {

    public static EntryListFragment newInstance(int pos) {
        Bundle b = new Bundle();
        EntryListFragment fragment = new EntryListFragment();
        b.putInt(KEY_POSITION, pos);
        fragment.setArguments(b);
        return fragment;
    }

    public static final String KEY_POSITION = "key_position";

    public TextView tvNoEntries;
    public RecyclerView entryList;
    public EntryListAdapter adapter;
    public boolean scrollEnabled = true;
    private boolean mChildCountZero = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_fragment_entry_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        tvNoEntries = getView().findViewById(R.id.tvNoEntries);
        entryList = getView().findViewById(R.id.entryList);

        LinearLayoutManager managerEntries =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
                    @Override
                    public boolean canScrollVertically() {
                        return scrollEnabled && !mChildCountZero;
                    }
                };
        managerEntries.setItemPrefetchEnabled(true);
        entryList.setLayoutManager(managerEntries);
        //entryList.addItemDecoration(new EntryItemDecoration(getContext()));

        adapter = new EntryListAdapter(getContext());
        entryList.setAdapter(adapter);

//        OverScrollDecoratorHelper.setUpOverScroll(entryList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        update();
    }

    public void update() {
        int position = getArguments().getInt(KEY_POSITION);
        Database.Entry[] entries = Database.findEntriesByCourseAndDate(
                DayManager.dates[position],
                Integer.parseInt(Database.courseDegree),
                Integer.parseInt(Database.courseNumber));
        if(entries == null) {
            entries = new Database.Entry[0];
        }
        if(entries.length == 0) {
            tvNoEntries.setVisibility(View.VISIBLE);
        } else {
            tvNoEntries.setVisibility(View.INVISIBLE);
        }
        mChildCountZero = entries.length == 0;
        adapter.setEntries(entries);
        Handler handler = new Handler();
        //prevent lags
        handler.post(() -> {
            entryList.scrollToPosition(entryList.getAdapter().getItemCount()-1);
        });
        handler.postDelayed(() -> {
            entryList.scrollToPosition(0);
        }, 100);
    }
}
