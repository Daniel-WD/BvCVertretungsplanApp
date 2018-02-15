package com.titaniel.bvcvertretungsplan.entry_list.entry_background;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.titaniel.bvcvertretungsplan.R;

public class EntryBackground extends View {

    private class MyOutlineProvider extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setConvexPath(outLine);
        }

    }

    private Path outLine;

    private Paint mPaint;

    private float mWidth, mHeight;

    public EntryBackground(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(ContextCompat.getColor(context, R.color.two));
        mPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        calcOutline();

        canvas.drawPath(outLine, mPaint);


    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);

        calcOutline();
        setOutlineProvider(new MyOutlineProvider());
        invalidate();
    }

    private void calcOutline() {
        float x = (float)(Math.tan(Math.toRadians(getRotation()))*(double)mHeight);
        outLine = new Path();

        outLine.moveTo(0, 0);
        outLine.lineTo(x, mHeight);
        outLine.lineTo(mWidth, mHeight);
        outLine.lineTo(mWidth-x, 0);
        outLine.lineTo(0, 0);
    }
}
