package com.titaniel.bvcvertretungsplan.day_pager;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.titaniel.bvcvertretungsplan.R;

public class DayPagerTransformer implements ViewPager.PageTransformer {

    private static final float FADE_SPEED = 3;
    private static final float BLOCK_VALUE = 0.9f;

    private float mRatio = 0;

    @Override
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 1) {
            view.setAlpha(1 - Math.abs(FADE_SPEED*position));

            view.setRotation((float)Math.toDegrees(Math.atan(mRatio)));

            view.setTranslationX(-pageWidth*position/* *BLOCK_VALUE*/);

            float xDist = position * pageWidth;

            view.setTranslationY(mRatio*xDist);

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
