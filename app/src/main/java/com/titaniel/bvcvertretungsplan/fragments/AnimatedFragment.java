package com.titaniel.bvcvertretungsplan.fragments;

import android.support.v4.app.Fragment;

public abstract class AnimatedFragment extends Fragment {

    public abstract void show(long delay);

    public abstract long hide(long delay);

}
