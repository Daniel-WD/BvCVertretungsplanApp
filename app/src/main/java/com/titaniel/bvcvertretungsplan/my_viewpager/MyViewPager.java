package com.titaniel.bvcvertretungsplan.my_viewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;

public class MyViewPager extends ViewPager {

    private ScrollerCustomDuration mScroller = null;
    private boolean mSwipealbe = true;

    public MyViewPager(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        postInitViewPager();
    }

    public MyViewPager(@NonNull Context context) {
        super(context);
        postInitViewPager();
    }

    private void postInitViewPager() {
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            mScroller = new ScrollerCustomDuration(getContext(),
                    (Interpolator) interpolator.get(null));
            scroller.set(this, mScroller);
        } catch (Exception e) {
        }
    }

    public void setScrollDurationFactor(double scrollFactor) {
        mScroller.setScrollDurationFactor(scrollFactor);
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
