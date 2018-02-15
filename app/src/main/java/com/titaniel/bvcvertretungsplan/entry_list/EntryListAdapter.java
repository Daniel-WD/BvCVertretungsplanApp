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

    private Database.Day mDay;
    private Context mContext;
    private float mDegrees;

    public EntryListAdapter(Context context, Database.Day day, float degrees) {
        mContext = context;
        mDay = day;
        mDegrees = degrees;
    }

    @Override
    public EntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_entry, parent, false);
        return new EntryHolder(v);
    }

    @Override
    public void onBindViewHolder(EntryHolder holder, int position) {
        Database.Entry entry = mDay.entries.get(position);
//        holder.tvCourse.setText(entry.course);
//        holder.tvHours.setText(entry.hours);
//        holder.tvLesson.setText(entry.lesson);
//        holder.tvTeacher.setText(entry.teacher);
//        holder.tvRoom.setText(entry.room);
//        holder.tvInfo.setText(entry.info);

        /*holder.background.post(() -> {
            holder.background.setRotation(mDegrees);
        });*/
    }

    @Override
    public int getItemCount() {
        return mDay.entries.size();
    }

    class EntryHolder extends RecyclerView.ViewHolder {

        TextView tvCourse, tvHours, tvLesson, tvTeacher, tvRoom, tvInfo;

        EntryHolder(View itemView) {
            super(itemView);

//            tvCourse = itemView.findViewById(R.id.tvCourse);
//            tvHours = itemView.findViewById(R.id.tvHours);
//            tvLesson = itemView.findViewById(R.id.tvLesson);
//            tvTeacher = itemView.findViewById(R.id.tvTeacher);
//            tvRoom = itemView.findViewById(R.id.tvRoom);
//            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
    }

}
