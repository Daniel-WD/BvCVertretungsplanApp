package com.titaniel.bvcvertretungsplan.main_activity.course_settings.course_picker;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.main_activity.MainActivity;
import com.titaniel.bvcvertretungsplan.main_activity.course_settings.CourseSettingsFragment;
import com.titaniel.bvcvertretungsplan.main_activity.course_settings.NumberAdapter;
import com.titaniel.bvcvertretungsplan.utils.ViewUtils;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;

public class CoursePickerFragment extends Fragment {

    private static final String[] sNumbers = {"1", "2", "3", "4", "5"};

    private View mRoot;
    private TextView mTvChooseCourse;
    private TextView mTvCourse;
    private RecyclerView mCourseSpecList;
    private Button mBtnLeft, mBtnRight;
    private Button mBtnBackCancel, mBtnSkip;
    private String mNumber = "1";
    private LinearLayout mLayoutChoice;

    private LinearLayout mLyStandard, mLyAdditional;

    private TextView mTvAdditional;
    private TextView mTvAdditionalInfo;
    private EditText mTCourse;
    private Button mBtnAdditionalBack;
    private Button mBtnNext;
    private Button mBtnComplete;
    private FlowLayout mLySelected;

    private Handler mHandler = new Handler();
    private CoursePickerManager mManager = new CoursePickerManager();

    private boolean mCancelBackState = true;

    private int mAdditionalAmount = 0;

    private ArrayList<String> mSavedCourses = new ArrayList<>();
    private ArrayList<Integer> mSkippedIndexes = new ArrayList<>();

    private boolean mButtonsBlocked = false;
    private CourseSettingsFragment mCourseSettingsFragment;
    private MainActivity mActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_picker, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCourseSettingsFragment = (CourseSettingsFragment) getParentFragment();
        mActivity = (MainActivity) getActivity();

        //init
        mRoot = getView();
        mTvChooseCourse = mRoot.findViewById(R.id.tvChooseCourse);
        mTvCourse = mRoot.findViewById(R.id.tvCourse);
        mCourseSpecList = mRoot.findViewById(R.id.courseSpecList);
        mBtnLeft = mRoot.findViewById(R.id.btnLeft);
        mBtnRight = mRoot.findViewById(R.id.btnRight);
        mBtnBackCancel = mRoot.findViewById(R.id.btnBackCancel);
        mBtnSkip = mRoot.findViewById(R.id.btnSkip);
        mLayoutChoice = mRoot.findViewById(R.id.lyChoice);
        mLyStandard = mRoot.findViewById(R.id.lyStandard);
        mLySelected = mRoot.findViewById(R.id.lySelected);

        mLyAdditional = mRoot.findViewById(R.id.lyAdditional);
        mTvAdditional = mRoot.findViewById(R.id.tvAdditional);
        mTvAdditionalInfo = mRoot.findViewById(R.id.tvAdditionalInfo);
        mTCourse = mRoot.findViewById(R.id.tCourse);
        mBtnAdditionalBack = mRoot.findViewById(R.id.btnAddBack);
        mBtnNext = mRoot.findViewById(R.id.btnNext);
        mBtnComplete = mRoot.findViewById(R.id.btnComplete);

        //list
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCourseSpecList.setLayoutManager(manager);
        mCourseSpecList.setHasFixedSize(true);
        mCourseSpecList.setAdapter(new NumberAdapter(getContext(), sNumbers, R.layout.course_picker_item_number, this::numberClicked));

        //button left
        mBtnLeft.setOnClickListener(v -> {
            if(mButtonsBlocked) return;
            addCourse(mBtnLeft.getText().toString().trim());
            if(mManager.hasNext()) {
                setCourseWithCoolAnimation(true, true);
            } else {
                mManager.nextCourse();
                switchAdditionalStd(true);
            }
        });

        //button right
        mBtnRight.setOnClickListener(v -> {
            if(mButtonsBlocked) return;
            addCourse(mBtnRight.getText().toString().trim());
            if(mManager.hasNext()) {
                setCourseWithCoolAnimation(true, true);
            } else {
                mManager.nextCourse();
                switchAdditionalStd(true);
            }
        });

        //button back
        mBtnBackCancel.setOnClickListener(v -> {
            if(mButtonsBlocked) return;
            if(mManager.hasPrevious()) { //back
                removeLastCourse();
                setCourseWithCoolAnimation(false, false);
            } else {
                long delay = hide(0);
                mCourseSettingsFragment.showFromPicker(delay);
            }
        });

        //button skip
        mBtnSkip.setOnClickListener(v -> {
            if(mButtonsBlocked) return;
            mSkippedIndexes.add(mManager.currentIndex());
            if(mManager.hasNext()) {
                setCourseWithCoolAnimation(true, false);
            } else {
                switchAdditionalStd(true);
                mManager.nextCourse();
            }
        });

        //additional layout

        //edittext course
        mTCourse.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().equals("")) {
                    mBtnNext.animate()
                            .setStartDelay(0)
                            .setDuration(200)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .alpha(1)
                            .start();
                } else {
                    mBtnNext.animate()
                            .setStartDelay(0)
                            .setDuration(200)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .alpha(0.5f)
                            .start();
                }
            }
        });

        //button next
        mBtnNext.setOnClickListener(v -> {
            if(mButtonsBlocked) return;
            if(mTCourse.getText().toString().trim().equals("")) return;
            mAdditionalAmount++;
            addCourse(mTCourse.getText().toString().trim());
            mTCourse.setText("");
        });

        //additional back
        mBtnAdditionalBack.setOnClickListener(v -> {
            if(mButtonsBlocked) return;
            removeLastCourse();
            if(mAdditionalAmount-- <= 0) {
                mAdditionalAmount = 0;
                mManager.previousCourse();
                switchAdditionalStd(false);
            }
        });

        //complete button
        mBtnComplete.setOnClickListener(v -> {
            if(mButtonsBlocked) return;
            Database.selectedCourses.clear();
            Database.selectedCourses.addAll(mSavedCourses);
            mCourseSettingsFragment.confirmChangesAndHide();
        });

        mRoot.setVisibility(View.INVISIBLE);
    }

    private void addCourse(String course) {
        if(mSavedCourses.size() == 0) {
            mLySelected.setVisibility(View.VISIBLE);
            mLySelected.setAlpha(0);
            mLySelected.animate()
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .start();
            mTvChooseCourse.animate()
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(0)
                    .start();
        }
        mSavedCourses.add(course);
        TextView child = new TextView(getContext());
        child.setText(course);
        child.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        child.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        child.setTextColor(ContextCompat.getColor(getContext(), R.color.selectedCourses));
        FlowLayout.LayoutParams params =
                new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(pixelsFromDp(8));
        params.setMarginEnd(pixelsFromDp(8));
        params.topMargin = pixelsFromDp(2);
        params.bottomMargin = pixelsFromDp(2);
        mLySelected.addView(child, params);
    }

    private void removeLastCourse() {
        if(mSavedCourses.size() == 0) return;
        if(mSkippedIndexes.contains(mManager.currentIndex() - 1 + mAdditionalAmount)) {
            mSkippedIndexes.remove(mSkippedIndexes.indexOf(mManager.currentIndex() - 1));
            return;
        }
        mSavedCourses.remove(mSavedCourses.size() - 1);
        if(mSavedCourses.size() == 0) {
            mLySelected.animate()
                    .setStartDelay(100)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(0)
                    .withEndAction(() -> mLySelected.setVisibility(View.INVISIBLE))
                    .start();
            mTvChooseCourse.animate()
                    .setStartDelay(100)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .start();
        }
        mLySelected.removeViewAt(mLySelected.getChildCount() - 1);
    }

    private void removeAllCourses() {
        mLySelected.animate()
                .setStartDelay(100)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .withEndAction(() -> mLySelected.setVisibility(View.INVISIBLE))
                .start();
        mTvChooseCourse.animate()
                .setStartDelay(100)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();
        mLySelected.removeAllViews();
    }

    private int pixelsFromDp(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void numberClicked(String number) {
        mNumber = number;

        long delay = 0;
        mBtnLeft.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(0)
                .start();
        mBtnRight.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(0)
                .start();
        delay += 120;
        mHandler.postDelayed(() -> {
            setCourse(mManager.currentCourse());
            mBtnLeft.animate()
                    .setStartDelay(0)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(1)
                    .start();
            mBtnRight.animate()
                    .setStartDelay(0)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(1)
                    .start();
        }, delay);
    }

    @SuppressLint("SetTextI18n")
    private void setCourse(CoursePickerManager.Course course) {
        mTvCourse.setText(course.name);
        mBtnLeft.setText(course.shortName.toUpperCase() + mNumber);
        mBtnRight.setText(course.shortName.toLowerCase() + mNumber);
        if(mManager.hasPrevious()) {
            switchCancelBackTo(false);
        } else {
            switchCancelBackTo(true);
        }
    }

    private void setCourseWithCoolAnimation(boolean next, boolean animate) {
        if(mButtonsBlocked) return;
        blockButtons(500);
        int width = mRoot.getWidth();
        long delay = 0;
        int dist1 = animate ? -width/8 : 0;
        mTvCourse.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(0)
                .start();
        mBtnRight.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .translationX(dist1)
                .alpha(0)
                .start();
        mBtnLeft.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .translationX(dist1)
                .alpha(0)
                .start();
        delay += 120;

        mHandler.postDelayed(() -> {
            //noinspection ConstantConditions
            setCourse(next ? mManager.nextCourse() : mManager.previousCourse());
            long dela = 20;
            int dist2 = animate ? width/8 : 0;
            mTvCourse.animate()
                    .setStartDelay(dela)
                    .setDuration(100)
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(1)
                    .start();
            mBtnRight.setTranslationX(dist2);
            mBtnRight.animate()
                    .setStartDelay(dela)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .translationX(0)
                    .alpha(1)
                    .start();
            mBtnLeft.setTranslationX(dist2);
            mBtnLeft.animate()
                    .setStartDelay(dela)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .translationX(0)
                    .alpha(1)
                    .start();
        }, delay);
    }

    private void switchCancelBackTo(boolean cancel) {
        if(mCancelBackState == cancel) return;
        mCancelBackState = cancel;
        float alpha = 0.4f;
        mBtnBackCancel.animate()
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(0)
                .start();
        mHandler.postDelayed(() -> {
            mBtnBackCancel.setText(cancel ? R.string.cancel : R.string.back);
            mBtnBackCancel.animate()
                    .setDuration(100)
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(alpha)
                    .start();
        }, 100);
    }

    private void switchAdditionalStd(boolean additional) {
        if(mButtonsBlocked) return;
        blockButtons(500);
        if(additional) {
            long delay = 0;
            mLyStandard.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new AccelerateInterpolator())
                    .alpha(0)
                    .withEndAction(() -> mLyStandard.setVisibility(View.INVISIBLE))
                    .start();
            delay += 150;
            mLyAdditional.setVisibility(View.VISIBLE);
            mLyAdditional.setAlpha(0);
            mLyAdditional.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(1)
                    .start();
        } else {
            long delay = 0;
            mLyAdditional.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new AccelerateInterpolator())
                    .alpha(0)
                    .withEndAction(() -> mLyAdditional.setVisibility(View.INVISIBLE))
                    .start();
            delay += 150;
            mLyStandard.setVisibility(View.VISIBLE);
            mLyStandard.setAlpha(0);
            mLyStandard.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(1)
                    .start();
        }
    }

    public void show(long delay) {
        mLyStandard.setVisibility(View.VISIBLE);
        mLyStandard.setAlpha(1);
        mLyAdditional.setVisibility(View.INVISIBLE);
        mLyAdditional.setAlpha(0);

        removeAllCourses();
        mSavedCourses.clear();
        mManager = new CoursePickerManager();

        mCourseSpecList.post(() -> {
            ((NumberAdapter) mCourseSpecList.getAdapter()).setNumber(mNumber);
            //noinspection ConstantConditions
            setCourse(mManager.currentCourse());
        });

        mRoot.setVisibility(View.VISIBLE);
        mRoot.setAlpha(0);
        mRoot.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();
    }

    public long hide(long delay) {
        mRoot.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .withEndAction(() -> mRoot.setVisibility(View.INVISIBLE))
                .start();
        return 200;
    }

    private void blockButtons(long duration) {
        if(mButtonsBlocked) return;
        mButtonsBlocked = true;
        mHandler.postDelayed(() -> mButtonsBlocked = false, duration);
    }

}
