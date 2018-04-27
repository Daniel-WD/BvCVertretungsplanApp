package com.titaniel.bvcvertretungsplan.main_activity._old_detail_fragment.list_pager.entry_list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

import java.util.ArrayList;
import java.util.Arrays;

public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.EntryHolder> {

    private ArrayList<Database.Entry> mEntries = new ArrayList<>();
    private Context mContext;
    private RecyclerView mRecyclerView;

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
//
        holder.dotLesson.setVisibility(entry.lessonChangeVisible);
        holder.dotTeacher.setVisibility(entry.teacherChangeVisible);
        holder.dotRoom.setVisibility(entry.roomChangeVisible);
//
        holder.tvSpec.setVisibility(entry.specVisible);
//        holder.tvTeacherChange.setVisibility(entry.teacherChangeVisible);
//        holder.tvRoomChange.setVisibility(entry.roomChangeVisible);
//        holder.tvLessonChange.setVisibility(entry.lessonChangeVisible);
        holder.tvBreakOut.setVisibility(entry.breakOutVisible);

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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    public void show(long delay) {
        long additonalDelay = 50;
        for(int i = 0; i < getItemCount(); i++) {
            EntryHolder holder = (EntryHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
            if(holder != null) {
                holder.show(delay);
                delay += additonalDelay;
            }
        }
    }

    public long hide(long delay) {

        //nur visible holders werden zurück gegeben von der recycler view
        long additonalDelay = 10;
        for(int i = getItemCount()-1; i >= 0; i--) {
            EntryHolder holder = (EntryHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
            if(holder != null) {
                holder.hide(delay);
                delay += additonalDelay;
            }
        }
        return delay;
    }

    class EntryHolder extends RecyclerView.ViewHolder {

        TextView tvRoom, tvSpec, tvHours, tvLesson, tvTeacher, tvInfo, tvBreakOut, tvTeacherChange, tvLessonChange, tvRoomChange;
        ImageView dotLesson, dotTeacher, dotRoom;

        EntryHolder(View itemView) {
            super(itemView);

            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvHours = itemView.findViewById(R.id.tvHours);
            tvLesson = itemView.findViewById(R.id.tvLesson);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvInfo = itemView.findViewById(R.id.tvInfo);


            dotLesson = itemView.findViewById(R.id.dotLesson);
            dotTeacher = itemView.findViewById(R.id.dotTeacher);
            dotRoom = itemView.findViewById(R.id.dotRoom);

//            itemView.setAlpha(0.01f);
            itemView.setVisibility(View.INVISIBLE);
        }

        void show(long delay) {
            itemView.setVisibility(View.VISIBLE);
            itemView.setTranslationY(100);
            itemView.setAlpha(0);
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(300)
                    .setInterpolator(new LinearOutSlowInInterpolator())
                    .alpha(1)
                    .translationY(0)
                    .start();
        }

        void hide(long delay) {
            itemView.animate()
                    .setStartDelay(delay)
                    .setDuration(100)
                    .setInterpolator(new FastOutLinearInInterpolator())
                    .alpha(0)
                    .translationY(100)
                    .withEndAction(() -> itemView.setVisibility(View.INVISIBLE))
                    .start();
        }

    }

}
