package com.titaniel.bvcvertretungsplan.fragments.class_settings_fragment;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;

/**
 * @author Daniel Weidensdörfer
 * Adapter für die beiden Listen der Klassenwahl und für die Nummer eines Kurses in der Kurswahl
 */
public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.NumberHolder> {

    /**
     * Schnittstelle für den Fall dass eine bestimmte Nummer geklickt wurde
     */
    public interface NumberListener {
        void onClick(String number);
    }

    private NumberListener mListener;

    private RecyclerView mList;
    private Context mContext;
    private String[] mNumbers;
    private int mLayout;

    private int mSelected = -1;
    private boolean mEnabled = true;

    private int mSelectedColor, mNotSelectedColor;

    /**
     * Konstruktor... Zum erstellen des Objekts :D
     * @param context Context
     * @param numbers Nummern, die die List enhalten soll
     * @param layout Das Layout für die Nummer
     *               Denn zum Beispiel sehen die Nummern der Klassenwahlliste anders aus als die Numemrn
     *               der Klassennummerwahlliste
     * @param listener NumberListener, siehe <code>NumberListener</code>
     */
    public NumberAdapter(Context context, String[] numbers, int layout, NumberListener listener) {
        this.mNumbers = numbers;
        this.mContext = context;
        mListener = listener;
        mLayout = layout;

        //colors
        mSelectedColor = ContextCompat.getColor(context, R.color.itemSelected);
        mNotSelectedColor = ContextCompat.getColor(context, R.color.itemNotSelected);
    }

    /*
     IM FOLGENDEN SEHEN SIE METHODEN DIE SEHR ADAPTER SPEZIFISCH SIND UND DAHER NUR VERSTANDEN
     WERDEN KÖNNEN, WENN MAN SICH MIT ANDROID BESCHÄFTIGT. AUS DIESEM GRUND LASSE ICH DIE ERLÄUTERUNG
     DER METHODEN HIER WEG, DA DIESE VIEL ZU UMFANGREICH WERDEN WÜRDEN
      */
    @NonNull
    @Override
    public NumberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(mLayout, parent, false);
        return new NumberHolder(v);
    }
    @Override
    public void onBindViewHolder(NumberHolder holder, int position) {
        holder.tvNumber.setText(mNumbers[position]);
    }
    @Override
    public int getItemCount() {
        return mNumbers.length;
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mList = recyclerView;
    }

    /**
     * Setzt die ausgewählte Nummer
     * @param number
     */
    public void setNumber(String number) {
        for(int i = 0; i < getItemCount(); i++) {
            NumberHolder holder = (NumberHolder) mList.findViewHolderForAdapterPosition(i);
            if(holder.tvNumber.getText().equals(number)) {
                holder.itemView.callOnClick();
                break;
            }
        }
    }

    /**
     * Ob Nummern gewählt werden können
     * @param enabled true wenn Nummern gewählt werden können, false andernfalls
     */
    public void setEnabled(boolean enabled) {
        if(mEnabled == enabled) return;
        mEnabled = enabled;
        NumberHolder cur = (NumberHolder) mList.findViewHolderForAdapterPosition(mSelected);
        if(enabled) {
            cur.select();
        } else {
            cur.deselect();
        }
    }

    /**
     * Nehmen wir  an es ist gerade die 3 gewählt. Diese Methode wechselt zum Bespiel zur Nummer 5
     * und animiert dabei die Deselektion der 3 und die Selektion der 5.
     * @param toPosition
     */
    private void changeSelected(int toPosition) {
        if(toPosition == mSelected) return;
        //deselect
        if(mSelected != -1) {
            NumberHolder oldHolder = (NumberHolder) mList.findViewHolderForAdapterPosition(mSelected);
            oldHolder.deselect();
        }

        //select
        NumberHolder newHolder = (NumberHolder) mList.findViewHolderForAdapterPosition(toPosition);
        newHolder.select();

        mSelected = toPosition;
    }

    /**
     * ...ebenfalls sehr Listenspezifisch für Android...
     * Enthält das Layout einer Nummer
     */
    class NumberHolder extends RecyclerView.ViewHolder {

        TextView tvNumber;

        /**
         * Konstruktor...
         * @param itemView die View des Layouts
         */
        NumberHolder(View itemView) {
            super(itemView);

            tvNumber = itemView.findViewById(R.id.number);

            itemView.setOnClickListener(v -> {
                if(!mEnabled || mSelected == getAdapterPosition()) return;
                mListener.onClick(tvNumber.getText().toString());
                changeSelected(getAdapterPosition());
            });
        }

        /**
         * Selektieren der Nummer
         */
        void select() {
            animateTextColor(mSelectedColor);
        }

        /**
         * Deselektieren der Nummer
         */
        void deselect() {
            animateTextColor(mNotSelectedColor);
        }

        /**
         * Textfarbe animieren
         * @param color Farbe zu der animiert werden soll
         */
        private void animateTextColor(int color) {
            ValueAnimator colorAnim = ValueAnimator.ofArgb(tvNumber.getTextColors().getDefaultColor(), color);
            colorAnim.addUpdateListener(animation -> {
                tvNumber.setTextColor((int)animation.getAnimatedValue());
            });
            colorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            colorAnim.setDuration(100);
            colorAnim.start();
        }
    }

}
