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

import com.titaniel.bvcvertretungsplan.R;

public class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.DayHolder> {

    private Handler mHandler = new Handler();

    private RecyclerView mList;
    private String[] mElements = {
            "MO",
            "DI",
            "MI",
            "DO",
            "FR"
    };
    
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
        holder.text.setText(mElements[position]);
    }

    @Override
    public int getItemCount() {
        return mElements.length;
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
            ValueAnimator shadowOn = ValueAnimator.ofFloat(0, 15);
            shadowOn.addUpdateListener(animation ->
                    text.setShadowLayer((float)animation.getAnimatedValue(), 0, 0, Color.WHITE));
            shadowOn.setInterpolator(new AccelerateDecelerateInterpolator());
            shadowOn.setDuration(150);
            shadowOn.start();

            itemView.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(1.6f)
                    .scaleX(1.6f)
                    .alpha(1)
                    .start();

            return 150;
        }

        long deselect() {
            ValueAnimator shadowOff = ValueAnimator.ofFloat(15, 0);
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
                    .alpha(0.7f)
                    .start();

            return 200;
        }
    }
}
