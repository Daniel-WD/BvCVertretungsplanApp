package com.titaniel.bvcvertretungsplan.main_activity.detail_fragment.header_pager;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.titaniel.bvcvertretungsplan.R;

public class HeaderPagerTransformer implements ViewPager.PageTransformer {

    private static final float FADE_SPEED = 3;
    private static final float BLOCK_VALUE = 0.1f;

    private float mRatio = 0;

    private View mTitle, mNumber, mDate;

    @Override
    public void transformPage(View view, float position) {
        mTitle = view.findViewById(R.id.dayText);
        mNumber = view.findViewById(R.id.tvDayInMonth);
        mDate = view.findViewById(R.id.tvMonthYear);

        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 1) {
            view.setAlpha(1);
            mTitle.setAlpha(1 - Math.abs(position) * 2);
            mNumber.setAlpha(1 - Math.abs(position) * 1.4f);
            mDate.setAlpha(1 - Math.abs(position) * 1.4f);

            view.setTranslationX(-pageWidth*position);

            mTitle.setTranslationX(position * pageWidth *0.15f);
            mNumber.setTranslationX(position * pageWidth *0.25f);
            mDate.setTranslationX(position * pageWidth *0.25f);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }

    public float getRatio() {
        return mRatio;
    }

    public void setRatio(float mRatio) {
        this.mRatio = mRatio;
    }
}
