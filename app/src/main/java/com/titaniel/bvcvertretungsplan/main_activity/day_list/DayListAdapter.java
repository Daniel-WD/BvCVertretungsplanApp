package com.titaniel.bvcvertretungsplan.main_activity.day_list;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.utils.DateManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

public class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.DayHolder> {

    private Context mContext;
    private RecyclerView mRecyclerView;

    private int mCount;

    private Handler mHandler = new Handler();

    public DayListAdapter(Context context) {
        mContext = context;
        mCount = DateManager.preparedDates.length;
    }

    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_day, parent, false);
        return new DayHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull DayHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        int color = DateManager.preparedColors[position];
        holder.tvDay.setText(DateManager.preparedCapsDayList[position]);

        //adatper
        EntryListAdapter adapter = new EntryListAdapter(mContext, Database.findEntriesByCourseAndDate(DateManager.preparedDates[position]));
        holder.entryList.setAdapter(adapter);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    public void show(long delay) {
        mRecyclerView.setVisibility(View.VISIBLE);
        long additionalDelay = 50;
        for(int i = 0; i < mCount; i++) {
            DayHolder holder = (DayHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
            if(holder != null) {
                holder.show(delay);
                delay += additionalDelay;
            }
        }
    }

    public void hide(long delay) {
        long additionalDelay = 0;
        for(int i = 0; i < mCount; i++) {
            DayHolder holder = (DayHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
            if(holder != null) {
                holder.hide(delay);
                delay += additionalDelay;
            }
        }
        mHandler.postDelayed(() -> {
            mRecyclerView.setVisibility(View.INVISIBLE);
        }, delay + 150);
    }

    static class DayHolder extends RecyclerView.ViewHolder {

        TextView tvDay;
        RecyclerView entryList;

        private LinearLayoutManager mLayoutManager;

        DayHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            entryList = itemView.findViewById(R.id.entryList);

            //layoutmanager
            mLayoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.VERTICAL, false);

            entryList.setLayoutManager(mLayoutManager);
            entryList.addItemDecoration(new EntryItemDecoration(itemView.getContext()));
        }

        void show(long delay) {
            itemView.setVisibility(View.VISIBLE);
            itemView.setAlpha(0);
            itemView.setTranslationY(50);
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(1)
                    .translationY(0)
                    .start();
        }

        void hide(long delay) {
            itemView.setVisibility(View.VISIBLE);
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(0)
                    .start();
        }

    }

}
