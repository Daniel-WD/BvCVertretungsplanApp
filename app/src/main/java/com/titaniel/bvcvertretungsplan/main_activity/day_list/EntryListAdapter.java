package com.titaniel.bvcvertretungsplan.main_activity.day_list;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DateManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

import java.util.ArrayList;
import java.util.Arrays;

public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.EntryHolder> {

    private Context mContext;
    private RecyclerView mRecyclerView;

    private Database.Entry[] mEntries;

    private final int sNotChangedColor;
    private final Typeface mLightFont, mRegularFont;

    public EntryListAdapter(Context context, Database.Entry[] entries) {
        mContext = context;
        mEntries = entries;

        //typefaces
        mLightFont = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mRegularFont = Typeface.create("sans-serif", Typeface.NORMAL);

        //colors
        sNotChangedColor = ContextCompat.getColor(context, R.color.notChangedColor);

    }

    @NonNull
    @Override
    public EntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_entry, parent, false);
        return new EntryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryHolder holder, int position) {

        EntryHolder entryHolder = (EntryHolder) holder;

        Database.Entry entry = mEntries[position];
//
//            entryHolder.dotLesson.setColorFilter(color);
//            entryHolder.dotTeacher.setColorFilter(color);
//            entryHolder.dotRoom.setColorFilter(color);

        entryHolder.dotLesson.setVisibility(View.INVISIBLE);
        entryHolder.dotTeacher.setVisibility(View.INVISIBLE);
        entryHolder.dotRoom.setVisibility(View.INVISIBLE);

//            entryHolder.dotLesson.setVisibility(entry.lessonChangeVisible);
//            entryHolder.dotTeacher.setVisibility(entry.teacherChangeVisible);
//            entryHolder.dotRoom.setVisibility(entry.roomChangeVisible);
//
        //entryHolder.tvSpec.setVisibility(entry.specVisible);
//        entryHolder.tvTeacherChange.setVisibility(entry.teacherChangeVisible);
//        entryHolder.tvRoomChange.setVisibility(entry.roomChangeVisible);
//        entryHolder.tvLessonChange.setVisibility(entry.lessonChangeVisible);
        //entryHolder.tvBreakOut.setVisibility(entry.breakOutVisible);

//        entryHolder.tvHours.setText(String.valueOf(position));
//        entryHolder.tvSpec.setText(String.valueOf(position));
//        entryHolder.tvLesson.setText(String.valueOf(position));
//        entryHolder.tvTeacher.setText(String.valueOf(position));
//        entryHolder.tvRoom.setText(String.valueOf(position));
//        entryHolder.tvInfo.setText(String.valueOf(position));

        entryHolder.tvHours.setText(entry.hoursText);
        entryHolder.tvLesson.setText(entry.lesson);
        entryHolder.tvTeacher.setText(entry.teacher);
        entryHolder.tvRoom.setText(entry.room);
        entryHolder.tvInfo.setText(entry.info);

        if(entry.lessonChange) {
            entryHolder.tvLesson.setTextColor(Color.WHITE);
            entryHolder.tvLesson.setTypeface(mRegularFont);
        } else {
            entryHolder.tvLesson.setTextColor(sNotChangedColor);
            entryHolder.tvLesson.setTypeface(mRegularFont);//todo
        }
        if(entry.roomChange) {
            entryHolder.tvRoom.setTextColor(Color.WHITE);
            entryHolder.tvRoom.setTypeface(mRegularFont);
        } else {
            entryHolder.tvRoom.setTextColor(sNotChangedColor);
            entryHolder.tvRoom.setTypeface(mRegularFont);//todo
        }
        if(entry.teacherChange) {
            entryHolder.tvTeacher.setTextColor(Color.WHITE);
            entryHolder.tvTeacher.setTypeface(mRegularFont);
        } else {
            entryHolder.tvTeacher.setTextColor(sNotChangedColor);
            entryHolder.tvTeacher.setTypeface(mRegularFont);//todo
        }

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return mEntries.length;
    }

    static class EntryHolder extends RecyclerView.ViewHolder {

        TextView tvRoom, tvSpec, tvHours, tvLesson, tvTeacher, tvInfo, tvBreakOut, tvTeacherChange, tvLessonChange, tvRoomChange;
        ImageView dotLesson, dotTeacher, dotRoom;
        View hoursDivider;

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

            hoursDivider = itemView.findViewById(R.id.dividerHours);
        }

    }

}
