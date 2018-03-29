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

    public static DayPagerFragment newInstance(String text, String month, String number) {
        Bundle b = new Bundle();
        b.putString(KEY_TEXT, text);
        b.putString(KEY_MONTH, month);
        b.putString(KEY_NUMBER, number);
        DayPagerFragment fragment = new DayPagerFragment();
        fragment.setArguments(b);
        return fragment;
    }

    public static final String KEY_TEXT = "key_text";
    public static final String KEY_NUMBER = "key_number";
    public static final String KEY_MONTH = "key_month";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_fragment_day, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        TextView dayText = getView().findViewById(R.id.dayText);
        TextView numberText = getView().findViewById(R.id.tvDayInMonth);
        TextView monthText = getView().findViewById(R.id.tvMonthYear);

        if(getArguments() != null) {
            dayText.setText(getArguments().getString(KEY_TEXT));
            numberText.setText(getArguments().getString(KEY_NUMBER));
            monthText.setText(getArguments().getString(KEY_MONTH));
        }

    }
}
