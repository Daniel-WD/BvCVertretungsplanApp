package com.titaniel.bvcvertretungsplan.main_activity.course_settings;

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

public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.NumberHolder> {

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

    public NumberAdapter(Context context, String[] numbers, int layout, NumberListener listener) {
        this.mNumbers = numbers;
        this.mContext = context;
        mListener = listener;
        mLayout = layout;

        //colors
        mSelectedColor = ContextCompat.getColor(context, R.color.itemSelected);
        mNotSelectedColor = ContextCompat.getColor(context, R.color.itemNotSelected);
    }

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

    public void setNumber(String number) {
        for(int i = 0; i < getItemCount(); i++) {
            NumberHolder holder = (NumberHolder) mList.findViewHolderForAdapterPosition(i);
            if(holder.tvNumber.getText().equals(number)) {
                holder.itemView.callOnClick();
                break;
            }
        }
    }

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

    class NumberHolder extends RecyclerView.ViewHolder {

        TextView tvNumber;

        NumberHolder(View itemView) {
            super(itemView);

            tvNumber = itemView.findViewById(R.id.number);

            itemView.setOnClickListener(v -> {
                if(!mEnabled || mSelected == getAdapterPosition()) return;
                mListener.onClick(tvNumber.getText().toString());
                changeSelected(getAdapterPosition());
            });
        }

        void select() {
            animateTextColor(mSelectedColor);
        }

        void deselect() {
            animateTextColor(mNotSelectedColor);
        }

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