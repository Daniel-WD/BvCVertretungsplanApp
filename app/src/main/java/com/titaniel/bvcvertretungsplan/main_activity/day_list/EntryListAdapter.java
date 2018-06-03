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

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

/**
 * @author Daniel Weidensdörfer
 * Adapter für die VP-Einträge in jedem Tag-Element
 */
public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.EntryHolder> {

    private Context mContext;
    private Database.Entry[] mEntries;

    /**
     * Konstruktor
     * @param context Context
     * @param entries Einträge Array
     */
    EntryListAdapter(Context context, Database.Entry[] entries) {
        mContext = context;
        mEntries = entries;
    }

    /*
 IM FOLGENDEN SEHEN SIE METHODEN DIE SEHR ADAPTER SPEZIFISCH SIND UND DAHER NUR VERSTANDEN
 WERDEN KÖNNEN, WENN MAN SICH MIT ANDROID BESCHÄFTIGT. AUS DIESEM GRUND LASSE ICH DIE ERLÄUTERUNG
 DER METHODEN HIER WEG, DA DIESE VIEL ZU UMFANGREICH WERDEN WÜRDEN
  */
    @NonNull
    @Override
    public EntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_entry, parent, false);
        return new EntryHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull EntryHolder holder, int position) {

        Database.Entry entry = mEntries[position];
        holder.tvSpec.setVisibility(entry.specVisible);

        holder.tvSpec.setText(entry.course.specification);
        holder.tvHours.setText(entry.hoursText);
        holder.tvLesson.setText(entry.lesson);
        holder.tvTeacher.setText(entry.teacher);
        holder.tvRoom.setText(entry.room);
        holder.tvInfo.setText(entry.info);
    }
    @Override
    public int getItemCount() {
        return mEntries.length;
    }

    /**
     * Repräsentiert einen VP Eintrag
     */
    static class EntryHolder extends RecyclerView.ViewHolder {

        TextView tvRoom, tvSpec, tvHours, tvLesson, tvTeacher, tvInfo;

        EntryHolder(View itemView) {
            super(itemView);

            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvHours = itemView.findViewById(R.id.tvHours);
            tvLesson = itemView.findViewById(R.id.tvLesson);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            tvSpec = itemView.findViewById(R.id.tvSpec);
        }

    }

}
