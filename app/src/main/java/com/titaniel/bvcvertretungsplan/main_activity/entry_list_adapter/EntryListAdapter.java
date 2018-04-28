package com.titaniel.bvcvertretungsplan.main_activity.entry_list_adapter;

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

public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.MyHolder> {

    private static final int TYPE_ENTRY = 0;
    private static final int TYPE_DAY = 1;

    private Context mContext;
    private RecyclerView mRecyclerView;

    private Database.Entry[][] mEntries;
    private ArrayList<Database.Entry> mEntryList = new ArrayList<>();
    private int[] mDayPositions;
    private int mCount;

    private final int sNotChangedColor;

    private final Typeface mLightFont, mRegularFont;

    public EntryListAdapter(Context context) {
        mContext = context;

        //typefaces
        mLightFont = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mRegularFont = Typeface.create("sans-serif", Typeface.NORMAL);

        //colors
        sNotChangedColor = ContextCompat.getColor(context, R.color.notChangedColor);

        int dayCount = DateManager.preparedCapsDayList.length;
        mEntries = new Database.Entry[dayCount][];

        //entries
        for(int i = 0; i < mEntries.length; i++) {
            mEntries[i] = Database.findEntriesByCourseAndDate(DateManager.preparedDates[i],
                    Integer.parseInt(Database.courseDegree),
                    Integer.parseInt(Database.courseNumber));
        }

        //day positions
        int dex = 0;
        mDayPositions = new int[mEntries.length];
        for(int i = 0; i < mDayPositions.length; i++) {
            mDayPositions[i] = dex;
            dex += mEntries[i].length+1;
        }

        //count
        mCount = mDayPositions.length;
        for(Database.Entry[] entry : mEntries) {
            mCount += entry.length;
        }

        //entry list
        for(Database.Entry[] entries : mEntries) {
            mEntryList.add(null);
            mEntryList.addAll(Arrays.asList(entries));
        }
    }

    @Override
    public int getItemViewType(int position) {
        for(int mDayPosition : mDayPositions) {
            if(position == mDayPosition) return TYPE_DAY;
        }
        return TYPE_ENTRY;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType) {
            case TYPE_DAY:
                View a = LayoutInflater.from(mContext).inflate(R.layout.list_item_day, parent, false);
                return new DayHolder(a);
            case TYPE_ENTRY:
                View b = LayoutInflater.from(mContext).inflate(R.layout.list_item_entry, parent, false);
                return new EntryHolder(b);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        int dayPosition = roundPositionToDayPosition(position);
        int dex = 0;
        for(int i = 0; i < mDayPositions.length; i++) {
            if(mDayPositions[i] == dayPosition) {
                dex = i;
                break;
            }
        }
        int color = DateManager.preparedColors[dex];

        if(holder instanceof EntryHolder) {
            EntryHolder entryHolder = (EntryHolder)holder;

            Database.Entry entry = mEntryList.get(position);
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

            entryHolder.hoursDivider.setBackgroundColor(color);

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
        } else if(holder instanceof DayHolder) {
            DayHolder dayHolder = (DayHolder) holder;

            //dayHolder.leftDivider.setBackgroundColor(color);
            //dayHolder.rightDivider.setBackgroundColor(color);

            //dayHolder.tvDay.setTextColor(color);
            dayHolder.tvDay.setText(DateManager.preparedCapsDayList[dex]);

            if(position == 0) dayHolder.leftDivider.setVisibility(View.GONE);
            else dayHolder.leftDivider.setVisibility(View.VISIBLE);
        }

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

    private int roundPositionToDayPosition(int position) {
        int res = 0;
        for(int dayPosition : mDayPositions) {
            if(dayPosition <= position) res = dayPosition;
        }
        return res;
    }

    static abstract class MyHolder extends RecyclerView.ViewHolder {

        MyHolder(View itemView) {
            super(itemView);
            //itemView.setVisibility(View.INVISIBLE);
        }

    }

    static class EntryHolder extends MyHolder {

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

    static class DayHolder extends MyHolder {

        TextView tvDay;
        View leftDivider, rightDivider;

        DayHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
//            leftDivider = itemView.findViewById(R.id.dividerLeft);
//            rightDivider = itemView.findViewById(R.id.dividerRight);
        }

    }

}
