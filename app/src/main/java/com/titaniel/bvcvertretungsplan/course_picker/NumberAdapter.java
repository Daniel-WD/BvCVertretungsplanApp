package com.titaniel.bvcvertretungsplan.course_picker;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
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

    private Handler mHandler = new Handler();
    private final String[] mNumbers;
    private final Context mContext;
    private final int mClickColor;
    private NumberListener mListener;
    private final int mLayout;
    private int mSelected = -1;
    private boolean mClickable = true;
    private RecyclerView mList;
    private boolean mEnabled = true;

    public NumberAdapter(RecyclerView list, String[] numbers, Context context, int clickColor, int layout, NumberListener listener) {
        mList = list;
        this.mNumbers = numbers;
        this.mContext = context;
        mClickColor = clickColor;
        mListener = listener;
        mLayout = layout;
    }

    public void setNumber(String number) {
        //tvNumber is null instead
        mHandler.postDelayed(() -> {
            for(int i = 0; i < getItemCount(); i++) {
                NumberHolder holder = (NumberHolder) mList.findViewHolderForAdapterPosition(i);
                if(holder.tvNumber.getText().equals(number)) {
                    holder.itemView.callOnClick();
                    break;
                }
            }
        }, 100);
    }

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

    private void disableClickable(long duration) {
        mClickable = false;
        mHandler.postDelayed(() -> mClickable = true, duration);
    }

    public void setEnabled(boolean enabled) {
        if(mEnabled == enabled) return;
        mEnabled = enabled;
        NumberHolder cur = (NumberHolder) mList.findViewHolderForAdapterPosition(mSelected);
        if(enabled) {
            disableClickable(cur.select());
        } else {
            disableClickable(cur.deselect());
        }
    }

    public void changeSelected(int toPosition) {
        if(toPosition == mSelected) return;
        //deselect
        if(mSelected != -1) {
            NumberHolder oldHolder = (NumberHolder) mList.findViewHolderForAdapterPosition(mSelected);
            disableClickable(oldHolder.deselect());
        }

        //select
        NumberHolder newHolder = (NumberHolder) mList.findViewHolderForAdapterPosition(toPosition);
        disableClickable(newHolder.select());

        mSelected = toPosition;
    }

    class NumberHolder extends RecyclerView.ViewHolder {

        TextView tvNumber;

        public NumberHolder(View itemView) {
            super(itemView);

            tvNumber = itemView.findViewById(R.id.number);

            itemView.setOnClickListener(v -> {
                if(!mClickable || !mEnabled) return;
                mListener.onClick(tvNumber.getText().toString());
                changeSelected(getAdapterPosition());
            });

            itemView.post(() -> {
                tvNumber.setPivotY(tvNumber.getHeight());
            });
        }

        long select() {
            ValueAnimator colorAnim = ValueAnimator.ofArgb(Color.WHITE, mClickColor);
            colorAnim.addUpdateListener(animation -> {
                tvNumber.setTextColor((int)animation.getAnimatedValue());
            });
            colorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            colorAnim.setDuration(200);
            colorAnim.start();

            tvNumber.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(1.3f)
                    .scaleX(1.3f)
                    .start();

            return 150;
        }

        long deselect() {
            ValueAnimator colorAnim = ValueAnimator.ofArgb(mClickColor, Color.WHITE);
            colorAnim.addUpdateListener(animation -> {
                tvNumber.setTextColor((int)animation.getAnimatedValue());
            });
            colorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            colorAnim.setDuration(200);
            colorAnim.start();

            tvNumber.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(1f)
                    .scaleX(1f)
                    .start();

            return 150;
        }
    }

}
