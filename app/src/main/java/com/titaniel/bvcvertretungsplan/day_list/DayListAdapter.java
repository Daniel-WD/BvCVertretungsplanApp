package com.titaniel.bvcvertretungsplan.day_list;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DayManager;
import com.titaniel.bvcvertretungsplan.R;

public class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.DayHolder> {

    private static final int MAX_GLOW = 10;
    private static final float MAX_SCALE = 1.5f;
    private static final float MIN_ALPHA = 0.7f;

    private Handler mHandler = new Handler();

    private RecyclerView mList;
    
    private int mSelected = 0;
    private boolean mClickable = true;

    public DayListAdapter(RecyclerView rv) {
        this.mList = rv;
    }

    private void disableClickable(long duration) {
        mClickable = false;
        mHandler.postDelayed(() -> mClickable = true, duration);
    }

    private void changeSelected(int toPosition) {
        if(toPosition == mSelected) return;
        //deselect
        DayHolder oldHolder = (DayHolder) mList.findViewHolderForAdapterPosition(mSelected);
        disableClickable(oldHolder.deselect());

        //select
        DayHolder newHolder = (DayHolder) mList.findViewHolderForAdapterPosition(toPosition);
        disableClickable(newHolder.select());

        mSelected = toPosition;
    }

    @Override
    public DayHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mList.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayHolder(v);
    }

    @Override
    public void onBindViewHolder(DayHolder holder, int position) {
        holder.text.setText(DayManager.shortDayList[position]);
    }

    @Override
    public int getItemCount() {
        return DayManager.shortDayList.length;
    }


    class DayHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView text;

        DayHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            itemView.setOnClickListener((view) -> {
                if(mClickable) changeSelected(getAdapterPosition());
            });

            itemView.post(() -> {
                if(getAdapterPosition() == 0) select();
            });

            text = (TextView) itemView;
        }

        long select() {
            ValueAnimator shadowOn = ValueAnimator.ofFloat(0, MAX_GLOW);
            shadowOn.addUpdateListener(animation ->
                    text.setShadowLayer((float)animation.getAnimatedValue(), 0, 0, Color.WHITE));
            shadowOn.setInterpolator(new AccelerateDecelerateInterpolator());
            shadowOn.setDuration(150);
            shadowOn.start();

            itemView.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(MAX_SCALE)
                    .scaleX(MAX_SCALE)
                    .alpha(1)
                    .start();

            return 150;
        }

        long deselect() {
            ValueAnimator shadowOff = ValueAnimator.ofFloat(MAX_GLOW, 0);
            shadowOff.addUpdateListener(animation ->
                    text.setShadowLayer((float)animation.getAnimatedValue(), 0, 0, Color.WHITE));
            shadowOff.setInterpolator(new AccelerateDecelerateInterpolator());
            shadowOff.setDuration(150);
            shadowOff.start();

            itemView.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(1f)
                    .scaleX(1f)
                    .alpha(MIN_ALPHA)
                    .start();

            return 150;
        }
    }
}
