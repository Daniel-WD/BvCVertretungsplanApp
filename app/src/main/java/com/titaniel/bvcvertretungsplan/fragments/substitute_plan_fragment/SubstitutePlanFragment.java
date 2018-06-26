package com.titaniel.bvcvertretungsplan.fragments.substitute_plan_fragment;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.fragments.AnimatedFragment;
import com.titaniel.bvcvertretungsplan.main_activity.MainActivity;
import com.titaniel.bvcvertretungsplan.fragments.substitute_plan_fragment.day_list.DayListAdapter;

public class SubstitutePlanFragment extends AnimatedFragment {

    private Runnable mRBringDayListToTop = new Runnable() {

        boolean wasNotIdle = false;

        @Override
        public void run() {
            if(mDayList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && wasNotIdle) {
                mDayList.scrollToPosition(0);

                mHandler.postDelayed(() -> {
                    show(0);
                }, 10);
            } else {
                wasNotIdle = true;
                mHandler.post(this);
            }
        }
    };

    private boolean mHeaderFadeEnabled = false;
    private boolean mHeaderVisible = true;
    private float mFixedFirstItemPosition;

    private View mRoot;
    private ImageView mIvTitle;
    private ImageView mIvEdit;
    private LinearLayout mLyClass;
    private TextView mTvClass;
    private View mLyNothing;

    private RecyclerView mDayList;
    private DayListAdapter mDayListAdapter;

    private MainActivity mActivity;

    private Handler mHandler = new Handler();

    private boolean mBlockButtons = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_substitute_plan, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mActivity = (MainActivity) getActivity();

        //init views
        mRoot = getView();
        mIvTitle = mRoot.findViewById(R.id.ivTit);
        mIvEdit = mRoot.findViewById(R.id.ivEdit);
        mDayList = mRoot.findViewById(R.id.dayList);
        mLyClass = mRoot.findViewById(R.id.lyClass);
        mTvClass = mRoot.findViewById(R.id.tvClass);
        mLyNothing = mRoot.findViewById(R.id.lyNothing);

        //day list
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        mDayList.setLayoutManager(mLayoutManager);
        mDayList.setHasFixedSize(true);
        mDayList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                View child = mDayList.getChildAt(0);
                if(recyclerView.getChildAdapterPosition(child) == 0) {
                    float y = child.getY();
                    onScroll(mFixedFirstItemPosition - y);
                    Log.d("TAG", " ---- " + (mFixedFirstItemPosition - y));
                }
                super.onScrolled(recyclerView, dx, dy);
            }

        });

        //ivEdit
        mIvEdit.setOnClickListener(v -> {
            if(!mHeaderVisible || mBlockButtons) return;
            blockButtons(800);
            if(mActivity.state == MainActivity.STATE_FM_SUBSTITUTE) {
                mActivity.showClassSettingsFragment(0, this);
            } else {
                mActivity.showSubstituteFragment(0, false, mActivity.classSettingsFragment);
            }
        });

    }

    /**
     * Wird aufgerufen, wenn die Liste gescrollt wird
     *
     * @param distance Wie weit die Liste gerade vom Ursprungszustand(ungescrollter Zustand) entfernt ist
     */
    private void onScroll(float distance) {
        if(!mHeaderFadeEnabled) return;
        if(distance == 0) {
            showHeader();
        } else {
            hideHeader();
        }
    }

    /**
     * Vestecken aller Komponenten über der Liste (wird aufgerufen wenn die Scrolldistanz der Liste größer als 0 ist)
     */
    private void hideHeader() {
        if(!mHeaderVisible) return;
        mHeaderVisible = false;
        long delay = 0;

        //edit
        mIvEdit.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .translationY(-100)
                .alpha(0)
                .start();

        //title
        delay += 10;
        mIvTitle.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .translationY(-100)
                .alpha(0)
                .start();

        //icon
        delay += 10;
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .translationY(-100)
                .alpha(0)
                .start();
    }

    /**
     * Anzeigen aller Komponenten über der Liste (wird aufgerufen wenn die Scrolldistanz der Liste gleich 0 ist)
     */
    private void showHeader() {
        if(mHeaderVisible) return;
        mHeaderVisible = true;
        long delay = 0;

        //ly class
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .translationY(0)
                .alpha(1)
                .start();

        //title
        delay += 30;
        mIvTitle.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .translationY(0)
                .alpha(1)
                .start();

        //edit
        delay += 30;
        mIvEdit.setVisibility(View.VISIBLE);
        mIvEdit.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .translationY(0)
                .alpha(1)
                .start();

    }

    /**
     * Füllen der Liste
     *
     * @param runnable Runnable, was nach der Befüllung der Liste aufgerufen werden soll
     */
    public void fillList(Runnable runnable) {
        mHeaderFadeEnabled = false;
        mDayListAdapter = new DayListAdapter(getContext());
        mDayList.setAdapter(mDayListAdapter);
        mDayList.post(() -> {
            if(mDayList.getChildCount() != 0 && mDayList.canScrollVertically(1)) {
                mFixedFirstItemPosition = mDayList.getChildAt(0).getY();
                mDayList.smoothScrollToPosition(mDayList.getAdapter().getItemCount() - 1);
                mHandler.post(mRBringDayListToTop);
            } else {
                if(runnable != null) runnable.run();
            }
        });
    }

    /**
     * Versteckt alle Hauptkomponenten und zeigt die Kurswahl an
     */
    public long animateHide(long delay) {
        //avd animation
        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.avd_filter_to_close, getContext().getTheme());
        mIvEdit.setImageDrawable(drawable);
        drawable.start();

        //class ly
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //title
        mIvTitle.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //tv nothing
        if(mLyNothing.getVisibility() == View.VISIBLE) {
            mLyNothing.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(0)
                    .start();
        } else {
            mLyNothing.setVisibility(View.INVISIBLE);
        }

        //daylist
        if(mDayListAdapter != null) mDayListAdapter.hide(delay);

        return 150;
    }

    /**
     * Wird aufgerufen wenn die Kurswahl geschlossen wurde und die Haupt-Komponenten wieder angezeigt
     * werden sollen
     *
     * @param delay Zeitverzögerung
     */
    public void animateShow(long delay) {

        updateClassText();

        if(mIvEdit.getVisibility() == View.INVISIBLE) {
            mIvEdit.setVisibility(View.VISIBLE);
            mIvEdit.setImageResource(R.drawable.ic_filter_variant);
            mIvEdit.setTranslationY(50);
            mIvEdit.setAlpha(0f);
            mIvEdit.animate()
                    .setStartDelay(delay)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .translationY(0)
                    .withEndAction(() -> mHeaderFadeEnabled = true)
                    .alpha(1)
                    .start();
        } else {
            //avd animation
            AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)
                    getResources().getDrawable(R.drawable.avd_close_to_filter, getContext().getTheme());
            mIvEdit.setImageDrawable(drawable);
            drawable.start();
        }

        //title
        delay += 50;
        mIvTitle.setVisibility(View.VISIBLE);
        mIvTitle.setTranslationY(50);
        mIvTitle.setAlpha(0f);
        mIvTitle.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .translationY(0)
                .withEndAction(() -> mHeaderFadeEnabled = true)
                .alpha(1)
                .start();

        //class ly
        delay += 50;
        mLyClass.setVisibility(View.VISIBLE);
        mLyClass.setTranslationY(50);
        mLyClass.setAlpha(0);
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .translationY(0)
                .alpha(1)
                .start();

        //tv nothing
        if(mDayList.getChildCount() == 0) {
            delay += 50;
            mLyNothing.setVisibility(View.VISIBLE);
            mLyNothing.setTranslationY(50);
            mLyNothing.setAlpha(0);
            mLyNothing.animate()
                    .setStartDelay(delay)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .translationY(0)
                    .alpha(1)
                    .start();
        } else {
            mLyNothing.setVisibility(View.INVISIBLE);
        }

        //list
        delay += 50;
        mDayList.setVisibility(View.VISIBLE);
        if(mDayListAdapter != null) mDayListAdapter.show(delay);
    }

    /**
     * Aktuallisieren der Klassenanzeige
     */
    private void updateClassText() {
        if(Integer.parseInt(Database.courseDegree) > 10) {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree));
        } else {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree + "/" + Database.courseNumber));
        }
    }

    private void blockButtons(long duration) {
        mBlockButtons = true;
        mHandler.postDelayed(() -> mBlockButtons = false, duration);
    }
}
