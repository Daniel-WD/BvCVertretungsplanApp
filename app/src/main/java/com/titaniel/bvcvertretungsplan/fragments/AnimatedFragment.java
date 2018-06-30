package com.titaniel.bvcvertretungsplan.fragments;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;

public abstract class AnimatedFragment extends Fragment {

    protected boolean isActive = false;

    private final Handler mHandler = new Handler();

    @Override
    public void onStart() {
        super.onStart();
//        if(!isActive) {
//            getView().setVisibility(View.INVISIBLE);
//        } else {
//            getView().setVisibility(View.VISIBLE);
//        }
    }

    public final void show(long delay) {
        isActive = true;
//        getView().setVisibility(View.VISIBLE);
        animateShow(delay);
    }

    public final long hide(long delay) {
        isActive = false;
        long duration = animateHide(delay);
//        mHandler.postDelayed(() -> getView().setVisibility(View.INVISIBLE), delay + duration);
        return duration;
    }

    public abstract void animateShow(long delay);
    public abstract long animateHide(long delay);

    public View[] ignoredViewsVisibility() {
        return null;
    }

}
