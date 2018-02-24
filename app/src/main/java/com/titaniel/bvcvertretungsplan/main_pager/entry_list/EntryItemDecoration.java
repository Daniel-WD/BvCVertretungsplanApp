package com.titaniel.bvcvertretungsplan.main_pager.entry_list;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.titaniel.bvcvertretungsplan.R;

public class EntryItemDecoration extends RecyclerView.ItemDecoration {

    private Paint mPaint;

    private static final float SCALE = 0.3f;

    public EntryItemDecoration(Context context) {
        int dividerColor = ContextCompat.getColor(context, R.color.snow);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        for(int i = 1; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            float y = child.getY() + mPaint.getStrokeWidth()/2;
            float dx = (c.getWidth()*(1-SCALE))/2;
            c.drawLine(dx, y, c.getWidth() -dx, y, mPaint);
        }

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = (int)(mPaint.getStrokeWidth()/2f);
    }
}
