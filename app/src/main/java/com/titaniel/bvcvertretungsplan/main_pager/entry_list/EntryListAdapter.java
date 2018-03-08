package com.titaniel.bvcvertretungsplan.main_pager.entry_list;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.EntryHolder> {

    private ArrayList<Database.Entry> mEntries = new ArrayList<>();
    private Context mContext;
    private Handler handler = new Handler();

    public EntryListAdapter(Context context, Database.Entry... entries) {
        mContext = context;
        setEntries(entries);
    }

    public void setEntries(Database.Entry... entries) {
        Arrays.sort(entries);
        mEntries.clear();
        mEntries.addAll(Arrays.asList(entries));
        notifyDataSetChanged();
    }

    @Override
    public EntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_entry, parent, false);
        return new EntryHolder(v);
    }

    @Override
    public void onBindViewHolder(EntryHolder holder, int position) {
        Database.Entry entry = mEntries.get(position);

        holder.dotLesson.setVisibility(entry.lessonDotVisible);
        holder.dotTeacher.setVisibility(entry.teacherDotVisible);
        holder.dotRoom.setVisibility(entry.roomDotVisible);
        holder.tvSpec.setVisibility(entry.specVisible);

//        holder.tvHours.setText(String.valueOf(position));
//        holder.tvSpec.setText(String.valueOf(position));
//        holder.tvLesson.setText(String.valueOf(position));
//        holder.tvTeacher.setText(String.valueOf(position));
//        holder.tvRoom.setText(String.valueOf(position));
//        holder.tvInfo.setText(String.valueOf(position));

        holder.tvHours.setText(entry.hoursText);
        holder.tvSpec.setText(entry.course.specification);
        holder.tvLesson.setText(entry.lesson);
        holder.tvTeacher.setText(entry.teacher);
        holder.tvRoom.setText(entry.room);
        holder.tvInfo.setText(entry.info);

    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    class EntryHolder extends RecyclerView.ViewHolder {

        TextView tvRoom, tvSpec, tvHours, tvLesson, tvTeacher, tvInfo;
        ImageView dotLesson, dotTeacher, dotRoom;

        EntryHolder(View itemView) {
            super(itemView);

            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvSpec = itemView.findViewById(R.id.tvSpec);
            tvHours = itemView.findViewById(R.id.tvHours);
            tvLesson = itemView.findViewById(R.id.tvLesson);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvInfo = itemView.findViewById(R.id.tvInfo);

            dotLesson = itemView.findViewById(R.id.dotLesson);
            dotTeacher = itemView.findViewById(R.id.dotTeacher);
            dotRoom = itemView.findViewById(R.id.dotRoom);
        }
    }

}
