package com.titaniel.bvcvertretungsplan;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

    public static void setNestedEnabled(View v, boolean clickable) {
        if(v == null) return;
        v.setClickable(clickable);
        v.setEnabled(clickable);
        v.setFocusable(false);
        if(v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            for(int i = 0; i < group.getChildCount(); i++) {
                setNestedEnabled(group.getChildAt(i), clickable);
            }
        }
    }

}
