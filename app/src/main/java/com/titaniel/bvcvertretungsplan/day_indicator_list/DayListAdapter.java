package com.titaniel.bvcvertretungsplan.day_indicator_list;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DayManager;
import com.titaniel.bvcvertretungsplan.R;

public class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.DayHolder> {

    private Handler mHandler = new Handler();

    private RecyclerView mList;
    private ViewPager mPager;
    
    private int mSelected = 0;
    private boolean mClickable = true;

    public DayListAdapter(RecyclerView rv, ViewPager dayPager) {
        this.mList = rv;
        this.mPager = dayPager;
    }

    private void disableClickable(long duration) {
        mClickable = false;
        mHandler.postDelayed(() -> mClickable = true, duration);
    }

    public void changeSelected(int toPosition) {
        if(toPosition == mSelected) return;
        //deselect
        DayHolder oldHolder = (DayHolder) mList.findViewHolderForAdapterPosition(mSelected);
        if(oldHolder != null) disableClickable(oldHolder.deselect());

        //select
        DayHolder newHolder = (DayHolder) mList.findViewHolderForAdapterPosition(toPosition);
        disableClickable(newHolder.select());

        mSelected = toPosition;
    }

    @Override
    public DayHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mList.getContext()).inflate(R.layout.list_item_day, parent, false);
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

        private static final int MAX_GLOW = 0;
        private static final float MAX_SCALE = 1.5f;
        private static final float MIN_ALPHA = 0.7f;

        View itemView;
        TextView text;

        DayHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            itemView.setOnClickListener((view) -> {
                if(mClickable) {
                    int pos = getAdapterPosition();
                    changeSelected(pos);
                    mPager.setCurrentItem(pos, true);
                }
            });

            itemView.post(() -> {
                if(getAdapterPosition() == 0) select();
            });

            text = itemView.findViewById(R.id.shortDay);

        }

        long select() {
            ValueAnimator shadowOn = ValueAnimator.ofFloat(0, MAX_GLOW);
            shadowOn.addUpdateListener(animation ->
                    text.setShadowLayer((float)animation.getAnimatedValue(), 0, 0, Color.WHITE));
            shadowOn.setInterpolator(new AccelerateDecelerateInterpolator());
            shadowOn.setDuration(150);
            shadowOn.start();

            text.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(MAX_SCALE)
                    .scaleX(MAX_SCALE)
                    .alpha(1)
                    .start();

            return 150;
        }

        long deselect() {
/*            line.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleX(0)
                    .start();*/

            ValueAnimator shadowOff = ValueAnimator.ofFloat(MAX_GLOW, 0);
            shadowOff.addUpdateListener(animation ->
                    text.setShadowLayer((float)animation.getAnimatedValue(), 0, 0, Color.WHITE));
            shadowOff.setInterpolator(new AccelerateDecelerateInterpolator());
            shadowOff.setDuration(150);
            shadowOff.start();

            text.animate()
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
