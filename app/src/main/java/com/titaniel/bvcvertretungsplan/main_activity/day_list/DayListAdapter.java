package com.titaniel.bvcvertretungsplan.main_activity.day_list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DateManager;
import com.titaniel.bvcvertretungsplan.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.DayHolder> {

    public interface DayListCallback {
        void onClick(int index, float x, float y, int width);
    }

    private DayListCallback mDayListCallback;

    public static final int[] POSITION_MAPPING = {0, 2, 4, 1, 3};
    public static final int[] POSITION_MAPPING_REVERSE = {0, 3, 1, 4, 2};

    private Integer[] mPosRedirect = {0, 3, 1, 4, 2};

    private Context mContext;
    private RecyclerView mRecyclerView;

    private Handler mHandler = new Handler();

    private ArrayList<Integer> mDisabled = new ArrayList<>();
    private ArrayList<Integer> mEnabled = new ArrayList<>();

    public DayListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private void calcPosRedirect() {
        Random r = new Random();

        mEnabled.clear();
        mDisabled.clear();

        for(int i = 0; i < getItemCount(); i++) {
            DayHolder holder = (DayHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
            if(holder.enabled) mEnabled.add(i); else mDisabled.add(i);
        }

        ArrayList<Integer> res = new ArrayList<>();

        randomizeList(mDisabled);
        randomizeList(mEnabled);
        res.addAll(mDisabled);
        res.addAll(mEnabled);

        mPosRedirect = res.toArray(mPosRedirect);
    }

    private void randomizeList(ArrayList<Integer> src) {
        ArrayList<Integer> res = new ArrayList<>();
        Random r = new Random();
        for(int i = 0; i < src.size(); i++) {
            Integer newNumber;
            do {
                newNumber = src.get(r.nextInt(src.size()));
            } while(res.contains(newNumber));
            res.add(newNumber);
        }
        src.clear();
        src.addAll(res);
    }

    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_day, parent, false);
        return new DayHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        if(position == 3)
            holder.itemView.post(() -> holder.itemView.setTranslationY(holder.ivBackground.getHeight()*3/4));
        if(position == 4)
            holder.itemView.post(() -> holder.itemView.setTranslationY(holder.ivBackground.getHeight()/4));
        position = POSITION_MAPPING[position];

        holder.tvDay.setText(DateManager.shortDayList[position]);

        //mEnabled
        holder.setEnabled(Arrays.asList(DateManager.preparedShortDayList).contains(DateManager.shortDayList[position]));
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void enter(long delay) {
        calcPosRedirect();
        for(Integer pos : mDisabled) {
            DayHolder holder = (DayHolder) mRecyclerView.findViewHolderForAdapterPosition(pos);
            holder.enter(delay);
        }

        delay += 200;
        long additionalDelay = 30;
        for(Integer pos : mEnabled) {
            DayHolder holder = (DayHolder) mRecyclerView.findViewHolderForAdapterPosition(pos);
            holder.enter(delay);
            delay += additionalDelay;
        }

        delay += 350;
        for(int i = 0; i < getItemCount(); i++) {
            DayHolder holder = (DayHolder) mRecyclerView.findViewHolderForAdapterPosition(mPosRedirect[i]);
            holder.markIfEnabled(delay);
        }
    }

    public void hide(long delay, int clickedIndex) {
        for(int i = 0; i < getItemCount(); i++) {
            DayHolder holder = (DayHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
            if(i != clickedIndex) holder.hide(delay);
            else holder.blow(delay);
        }
    }

    public void show(long delay, int clickedIndex) {
        for(int i = 0; i < getItemCount(); i++) {
            DayHolder holder = (DayHolder) mRecyclerView.findViewHolderForAdapterPosition(POSITION_MAPPING_REVERSE[i]);
            if(i != clickedIndex) holder.show(delay);
            else holder.deBlow(delay);
        }
    }

    public class DayHolder extends RecyclerView.ViewHolder {

        private static final float DISABLED_SCALE = 0.9f;

        private boolean enabled;
        ImageView ivBackground;
        TextView tvDay;

        @SuppressLint("ClickableViewAccessibility")
        DayHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            ivBackground = itemView.findViewById(R.id.ivBg);

            tvDay.setVisibility(View.INVISIBLE);
            ivBackground.setVisibility(View.INVISIBLE);

            itemView.setOnClickListener((v) -> {
                if(!enabled) return;
                if(mDayListCallback != null) {
                    int[] loc = new int[2];
                    itemView.getLocationOnScreen(loc);
                    int w = itemView.getWidth();
                    int index = getAdapterPosition();
                    mDayListCallback.onClick(index,
                            loc[0]+w/2, loc[1]+w/2, w);
                }
            });
        }

        public int[] location() {
            float sx = itemView.getScaleX(), sy = itemView.getScaleY();
            itemView.setScaleX(1);
            itemView.setScaleY(1);
            int[] loc = new int[2];
            itemView.getLocationOnScreen(loc);
            itemView.setScaleX(sx);
            itemView.setScaleY(sy);
            return loc;
        }

        public int diameter() {
            itemView.setScaleX(1);
            itemView.setScaleY(1);
            return itemView.getWidth();
        }

        void setEnabled(boolean enabled) {
            Drawable background;
            if(enabled) {
                background = mContext.getDrawable(R.drawable.avd_day_border_enabled_two).getConstantState().newDrawable().mutate();
                tvDay.setAlpha(1);
                itemView.setScaleX(1);
                itemView.setScaleY(1);
            } else {
                background = mContext.getDrawable(R.drawable.day_background_disabled);
                tvDay.setAlpha(0.7f);
                itemView.setScaleX(DISABLED_SCALE);
                itemView.setScaleY(DISABLED_SCALE);
            }
            ivBackground.setImageDrawable(background);
            this.enabled = enabled;
        }

        void enter(long delay) {
            ivBackground.setVisibility(View.VISIBLE);
            tvDay.setVisibility(View.VISIBLE);
            itemView.setVisibility(View.VISIBLE);
            itemView.setAlpha(0f);
            itemView.setScaleX(enabled ? 0.5f : 0.5f);
            itemView.setScaleY(enabled ? 0.5f : 0.5f);
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(enabled ? 500 : 400)
                    .setInterpolator(enabled ? new OvershootInterpolator(2) : new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .scaleX(enabled ? 1 : DISABLED_SCALE)
                    .scaleY(enabled ? 1 : DISABLED_SCALE)
                    .start();
        }

        void markIfEnabled(long delay) {
            mHandler.postDelayed(() -> {
                if(ivBackground.getDrawable() instanceof AnimatedVectorDrawable)
                    ((AnimatedVectorDrawable) ivBackground.getDrawable()).start();
            }, delay);
        }

        void hide(long delay) {
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(0)
                    .start();
        }
        void show(long delay) {
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .scaleX(enabled ? 1 : DISABLED_SCALE)
                    .scaleY(enabled ? 1 : DISABLED_SCALE)
                    .start();
        }

        void blow(long delay) {
            itemView.setAlpha(0);
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(70)
                    .setInterpolator(new AccelerateInterpolator())
                    .alpha(0)
                    .start();
        }
        void deBlow(long delay) {
            itemView.setScaleX(0.2f);
            itemView.setScaleY(0.2f);
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(350)
                    .setInterpolator(new OvershootInterpolator(2))
                    .alpha(1)
                    .scaleX(1f)
                    .scaleY(1f)
                    .start();
        }

    }

    public void setDayListCallback(DayListCallback callback) {
        mDayListCallback = callback;
    }

}
