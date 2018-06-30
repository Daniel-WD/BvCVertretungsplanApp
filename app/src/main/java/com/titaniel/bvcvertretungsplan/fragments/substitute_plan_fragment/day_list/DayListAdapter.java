package com.titaniel.bvcvertretungsplan.fragments.substitute_plan_fragment.day_list;


import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.date_manager.DateManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

/**
 * @author Daniel Weidensdörfer
 * Listenadapter für die Tage, die jeweils entsprechende VP-Einträge enthalten... also Montag, Dienstag...
 */
public class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.DayHolder> {

    private Context mContext;
    private RecyclerView mRecyclerView;

    private int mCount;

    private Handler mHandler = new Handler();

    /**
     * Konstruktor, also Initialisierungen
     * @param context
     */
    public DayListAdapter(Context context) {
        mContext = context;
        mCount = DateManager.preparedDates.length;
    }

    /*
     IM FOLGENDEN SEHEN SIE METHODEN DIE SEHR ADAPTER SPEZIFISCH SIND UND DAHER NUR VERSTANDEN
     WERDEN KÖNNEN, WENN MAN SICH MIT ANDROID BESCHÄFTIGT. AUS DIESEM GRUND LASSE ICH DIE ERLÄUTERUNG
     DER METHODEN HIER WEG, DA DIESE VIEL ZU UMFANGREICH WERDEN WÜRDEN
      */
    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_substitute_day, parent, false);
        return new DayHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
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

    /**
     * Animiertes Anzeigen aller Tage mit ihrem Inhalt
     * @param delay Zeitverzögerung
     */
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

    /**
     * Animiertes "Verstecken" aller Tage
     * @param delay Zeitverzögerung
     */
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

    /**
     * Repräsentiert einen Tag und enthält eine Liste mit Einträgen
     */
    static class DayHolder extends RecyclerView.ViewHolder {

        TextView tvDay;
        RecyclerView entryList;

        private LinearLayoutManager mLayoutManager;

        /**
         * Konstruktor
         * @param itemView Die View, die einen Tag repräsentiert
         */
        DayHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            entryList = itemView.findViewById(R.id.entryList);

            //layoutmanager
            mLayoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.VERTICAL, false);

            entryList.setLayoutManager(mLayoutManager);
            entryList.addItemDecoration(new EntryItemDecoration(itemView.getContext()));
        }

        /**
         * Anzeigen des Tages
         * @param delay Zeitverzögerung
         */
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

        /**
         * Verstecken des Tages
         * @param delay Zeitverzögerung
         */
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
