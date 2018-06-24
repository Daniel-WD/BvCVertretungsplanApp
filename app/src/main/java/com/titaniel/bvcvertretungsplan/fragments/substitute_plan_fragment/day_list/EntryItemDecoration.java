package com.titaniel.bvcvertretungsplan.fragments.substitute_plan_fragment.day_list;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.titaniel.bvcvertretungsplan.R;

/**
 * @author Daniel Weidensdörfer
 * Helferklasse, von android gestellt
 * Ist dafür da, Linien zwischen den VP-Einträgen zu zeichenen (fürs Design)
 */
public class EntryItemDecoration extends RecyclerView.ItemDecoration {

    private Paint mDividerPaint;
    private final float endMargin, startMargin; //linker und rechter Abstand der Linie zum Rand

    /**
     * Konstruktor
     * @param context
     */
    EntryItemDecoration(Context context) {
        int dividerColor = ContextCompat.getColor(context, R.color.dividerColor);

        endMargin = context.getResources().getDimensionPixelSize(R.dimen.dividerEndMargin);
        startMargin = context.getResources().getDimensionPixelSize(R.dimen.dividerStartMargin);

        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDividerPaint.setColor(dividerColor);
        mDividerPaint.setStyle(Paint.Style.STROKE);
        mDividerPaint.setStrokeWidth(1);
    }

    /**
     * Zeichnen der Linien auf die List
     * @param c Canvas... also die Zeichenoberfläche
     * @param parent RecyclerView... die Liste
     * @param state Status der Liste... (im Scroll, im Aus-Scrollen, oder Nicht-Scrollend)
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        for(int i = 0; i < parent.getChildCount() - 1; i++) {
            View child = parent.getChildAt(i);
            float y = child.getY() - mDividerPaint.getStrokeWidth()/2 + child.getHeight();
            c.drawLine(startMargin, y, c.getWidth() - endMargin, y, mDividerPaint);
        }
    }

    /**
     * Methode um der Liste zu "sagen", welchen zusätzlichen Abstand die Dekorationen (in diesem Fall die Linie)
     * pro Element einnehmen
     * @param outRect Rect um abstände mitzuteilen
     * @param view View, jeweiliges Listenelement
     * @param parent RecyclerView... die Liste
     * @param state Status der Liste
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = (int) (mDividerPaint.getStrokeWidth()/2f);
    }
}
