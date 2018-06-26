package com.titaniel.bvcvertretungsplan.fragments;

import android.support.v4.app.Fragment;

public abstract class AnimatedFragment extends Fragment {

    protected boolean isActive = false;

    public final void show(long delay) {
        isActive = true;
        animateShow(delay);
    }

    public final long hide(long delay) {
        isActive = false;
        return animateHide(delay);
    }

    public abstract void animateShow(long delay);
    public abstract long animateHide(long delay);

}
