package com.titaniel.bvcvertretungsplan.day_indicator_list;

import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
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

    public void setDot(int pos, boolean visible) {
        DayHolder holder = ((DayHolder) mList.findViewHolderForAdapterPosition(pos));
        holder.ivDot.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
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

    public class DayHolder extends RecyclerView.ViewHolder {

        private static final float MAX_SCALE = 1.2f;
        private static final float MIN_ALPHA = 0.7f;

        ImageView ivDot, ivCircle;
        View itemView;
        public TextView text;

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
            ivDot = itemView.findViewById(R.id.dot);
            ivCircle = itemView.findViewById(R.id.ivCircle);

            ivDot.setVisibility(View.INVISIBLE);
        }

        long select() {
            text.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(MAX_SCALE)
                    .scaleX(MAX_SCALE)
                    .alpha(1)
                    .start();
            ivDot.animate()
                    .setInterpolator(new OvershootInterpolator(8))
                    .setDuration(350)
                    .translationY(-10)
                    .alpha(1)
                    .start();

            ivCircle.setAlpha(0f);
            ivCircle.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .alpha(1)
                    .start();

            return 350;
        }

        long deselect() {
            text.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(1f)
                    .scaleX(1f)
                    .alpha(MIN_ALPHA)
                    .start();
            ivDot.animate()
                    .setInterpolator(new AnticipateInterpolator(8))
                    .setDuration(350)
                    .translationY(0)
                    .alpha(MIN_ALPHA)
                    .start();

            ivCircle.setAlpha(1f);
            ivCircle.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .alpha(0)
                    .start();

            return 350;
        }
    }
}
