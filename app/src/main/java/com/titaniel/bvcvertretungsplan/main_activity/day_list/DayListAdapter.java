package com.titaniel.bvcvertretungsplan.main_activity.day_list;


import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DateManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

import java.util.Arrays;

public class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.DayHolder> {

    private Context mContext;
    private RecyclerView mRecyclerView;

    private int mCount;

    private final int sNotChangedColor;
    private final Typeface mLightFont, mRegularFont;

    public DayListAdapter(Context context) {
        mContext = context;

        //typefaces
        mLightFont = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mRegularFont = Typeface.create("sans-serif", Typeface.NORMAL);

        //colors
        sNotChangedColor = ContextCompat.getColor(context, R.color.notChangedColor);

        mCount = DateManager.preparedDates.length;
    }

    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_day, parent, false);
        return new DayHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        int color = DateManager.preparedColors[position];

        holder.tvDay.setText(DateManager.preparedCapsDayList[position]);

        if(position == 0) holder.leftDivider.setVisibility(View.GONE);
        else holder.leftDivider.setVisibility(View.VISIBLE);

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

    static class DayHolder extends RecyclerView.ViewHolder {

        TextView tvDay;
        View leftDivider, rightDivider;

        DayHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            leftDivider = itemView.findViewById(R.id.dividerLeft);
            rightDivider = itemView.findViewById(R.id.dividerRight);
        }

    }

}
