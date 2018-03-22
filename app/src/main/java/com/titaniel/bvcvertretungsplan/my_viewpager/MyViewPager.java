package com.titaniel.bvcvertretungsplan.my_viewpager;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {

    private boolean mSwipealbe = true;

    public MyViewPager(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mSwipealbe && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mSwipealbe && super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mSwipealbe = enabled;
        super.setEnabled(enabled);
    }
}
