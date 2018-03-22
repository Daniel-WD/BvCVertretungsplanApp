package com.titaniel.bvcvertretungsplan.date_shower;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DayManager;
import com.titaniel.bvcvertretungsplan.R;

public class DateShower {

    public TextView mTvPrim, mTvSec;

    private float mTransY = 0;

    private int mLastIndex = -1;

    public DateShower(View container) {
        mTvPrim = container.findViewById(R.id.tvDatePrim);
        mTvSec = container.findViewById(R.id.tvDateSec);

        mTransY = 0.5f*mTvPrim.getHeight();
        show(0);
    }

    public void show(int index) {
        if(index == mLastIndex) return;
        String date = DayManager.dateList[index];
        if(index < mLastIndex) {
            move(date, -mTransY); //down
        } else {
            move(date, mTransY); //up
        }
        mLastIndex = index;
    }

    private void move(String text, float transY) {
        mTvSec.setText(mTvPrim.getText());
        mTvSec.setAlpha(1);
        mTvSec.setTranslationY(0);

        mTvPrim.setText(text);
        mTvPrim.setTranslationY(transY);
        mTvPrim.setAlpha(0);

        mTvSec.animate()
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .translationY(-transY)
                .start();

        mTvPrim.animate()
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();
    }

}
