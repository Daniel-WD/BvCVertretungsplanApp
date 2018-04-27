package com.titaniel.bvcvertretungsplan.main_activity.entry_list_adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.titaniel.bvcvertretungsplan.R;

public class EntryItemDecoration extends RecyclerView.ItemDecoration {

    private Paint mDividerPaint, mBottomLinePaint;

    private static final float SCALE = 0.95f;

    public EntryItemDecoration(Context context) {
        int dividerColor = ContextCompat.getColor(context, R.color.dividerColor);

        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDividerPaint.setColor(dividerColor);
        mDividerPaint.setStyle(Paint.Style.STROKE);
        mDividerPaint.setStrokeWidth(1);

        mBottomLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBottomLinePaint.setColor(Color.WHITE);
        mBottomLinePaint.setStyle(Paint.Style.STROKE);
        mBottomLinePaint.setStrokeWidth(2);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        for(int i = 0; i < parent.getChildCount()-1; i++) {
            View child = parent.getChildAt(i);
            View nextChild = parent.getChildAt(i+1);
            RecyclerView.ViewHolder holder = parent.findContainingViewHolder(child);
            RecyclerView.ViewHolder nextHolder = parent.findContainingViewHolder(nextChild);
            if(holder instanceof EntryListAdapter.EntryHolder && !(nextHolder instanceof EntryListAdapter.DayHolder)) {
                float y = child.getY() - mDividerPaint.getStrokeWidth()/2 + child.getHeight();
                float dx = (c.getWidth()*(1-SCALE))/2;
                c.drawLine(dx, y, c.getWidth() -dx, y, mDividerPaint);
            }
        }

        //bottom line
        View child = parent.getChildAt(parent.getChildCount()-1);
        if(child != null) {
            RecyclerView.ViewHolder holder = parent.findContainingViewHolder(child);
            if(holder != null) {
                float y = child.getY() - mBottomLinePaint.getStrokeWidth()/2 + child.getHeight();
                c.drawLine(0, y, c.getWidth(), y, mBottomLinePaint);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = (int)(mDividerPaint.getStrokeWidth()/2f);
    }
}
