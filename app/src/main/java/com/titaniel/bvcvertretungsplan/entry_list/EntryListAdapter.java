package com.titaniel.bvcvertretungsplan.entry_list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.EntryHolder> {

    private Database.Entry[] mEntries;
    private Context mContext;
    private float mDegrees;

    public EntryListAdapter(Context context, Database.Entry... entries) {
        mContext = context;
        mEntries = entries;
    }

    @Override
    public EntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_entry, parent, false);
        return new EntryHolder(v);
    }

    @Override
    public void onBindViewHolder(EntryHolder holder, int position) {
        Database.Entry entry = mEntries[position];
        if(entry.hours.startHour == entry.hours.endHour) {
            holder.tvHours.setText(String.valueOf(entry.hours.startHour));
        } else {
            // TODO: 20.02.2018 template
            holder.tvHours.setText(String.valueOf(entry.hours.startHour) + "-" + String.valueOf(entry.hours.endHour));
        }
        if(entry.course.specification.equals("")) {
            holder.tvSpec.setVisibility(View.GONE);
        } else {
            holder.tvSpec.setText(entry.course.specification);
        }
        holder.tvSpec.setText(entry.course.specification);
        holder.tvLesson.setText(entry.lesson);
        holder.tvTeacher.setText(entry.teacher);
        holder.tvRoom.setText(entry.room);
        if(entry.info == null || entry.info.equals("")) {
            holder.tvInfo.setVisibility(View.GONE);
        } else {
            holder.tvInfo.setText(entry.info);
        }
    }

    @Override
    public int getItemCount() {
        return mEntries.length;
    }

    class EntryHolder extends RecyclerView.ViewHolder {

        TextView tvRoom, tvSpec, tvHours, tvLesson, tvTeacher, tvInfo;

        EntryHolder(View itemView) {
            super(itemView);

            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvSpec = itemView.findViewById(R.id.tvSpec);
            tvHours = itemView.findViewById(R.id.tvHours);
            tvLesson = itemView.findViewById(R.id.tvLesson);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
    }

}
