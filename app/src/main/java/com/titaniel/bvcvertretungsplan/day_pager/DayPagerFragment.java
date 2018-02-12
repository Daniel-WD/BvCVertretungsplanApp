package com.titaniel.bvcvertretungsplan.day_pager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;

public class DayPagerFragment extends Fragment {

    public static DayPagerFragment newInstance(String text) {
        Bundle b = new Bundle();
        b.putString(KEY_TEXT, text);
        DayPagerFragment fragment = new DayPagerFragment();
        fragment.setArguments(b);
        return fragment;
    }

    public static final String KEY_TEXT = "key_text";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_fragment_day, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        TextView dayText = (TextView) getView().findViewById(R.id.dayText);

        if(getArguments() != null) {
            dayText.setText(getArguments().getString(KEY_TEXT));
        }

    }
}
