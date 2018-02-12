package com.titaniel.bvcvertretungsplan.date_shower;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DayManager;
import com.titaniel.bvcvertretungsplan.R;

public class DateShower {

    private TextView mTvPrim, mTvSec;

    private boolean mPrimSelected = true;
    private float mTransY = 0;

    public DateShower(View container) {
        mTvPrim = container.findViewById(R.id.tvDatePrim);
        mTvSec = container.findViewById(R.id.tvDateSec);

        mTransY = 0.5f*mTvPrim.getHeight();
        show(0);
    }

    public void show(int index) {
        String date = DayManager.dateList[index];

        if(mPrimSelected) {
            mTvSec.setText(date);
            swap(mTvPrim, mTvSec);
        } else {
            mTvPrim.setText(date);
            swap(mTvSec, mTvPrim);
        }
        mPrimSelected = !mPrimSelected;
    }

    private void swap(View vUp, View vDown) {
        //prim text up
        vUp.setTranslationY(0);
        vUp.setAlpha(1);
        vUp.animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200)
                .withEndAction(() -> vUp.setVisibility(View.VISIBLE))
                .alpha(0)
                .translationY(mTransY)
                .start();

        //sec text down
        vDown.setVisibility(View.VISIBLE);
        vDown.setTranslationY(mTransY);
        vDown.setAlpha(0);
        vDown.animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200)
                .alpha(1)
                .translationY(0)
                .start();
    }

}
