package com.titaniel.bvcvertretungsplan.main_activity.course_settings_fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DateManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.main_activity.MainActivity;

public class CourseSettingsFragment extends Fragment {

    private static final String[] sDegrees = {"5", "6", "7", "8", "9", "10", "11", "12"};
    private static final String[] sNumbers = {"1", "2", "3", "4", "5", "6"};

    private MainActivity mActivity;

    private View mRoot;
    private RecyclerView mCourseDegreeList, mCourseNumberList;
    private TextView mTvDegree, mTvNumber;
    private Button mBtnBack, mBtnOk, mBtnCourses;
    private ImageView mIvClose;

    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_settings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mActivity = (MainActivity) getContext();

        //init
        mRoot = getView();
        mCourseDegreeList = mRoot.findViewById(R.id.courseDegreeList);
        mCourseNumberList = mRoot.findViewById(R.id.courseNumberList);
        mTvDegree = mRoot.findViewById(R.id.tvCourseDegree);
        mTvNumber = mRoot.findViewById(R.id.tvCourseNumber);
        mBtnBack = mRoot.findViewById(R.id.btnBack);
        mBtnOk = mRoot.findViewById(R.id.btnOk);
        mBtnCourses = mRoot.findViewById(R.id.btnCourses);
        mIvClose = mRoot.findViewById(R.id.ivClose);

        //iv close
        mIvClose.setOnClickListener((v) -> {
            mActivity.onBackPressed();
        });

        //btn back
        mBtnBack.setOnClickListener((v) -> {
            mActivity.onBackPressed();
        });

        //btn Ok
        mBtnOk.setOnClickListener((v) -> {
            String newDegree = mTvDegree.toString();
            String newNumber = mTvNumber.toString();
            if(!Database.courseNumber.equals(newNumber) || !Database.courseDegree.equals(newDegree)) {
                Database.courseDegree = mTvDegree.getText().toString();
                Database.courseNumber = mTvNumber.getText().toString();
                DateManager.prepare();
                mActivity.fillList();
            }
            mActivity.onBackPressed();
        });

        //btn courses
        mBtnCourses.setOnClickListener((v) -> {
            mBtnOk.callOnClick();
        });


        //btn courses

        //mCourseDegreeList
        LinearLayoutManager managerDegrees =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCourseDegreeList.setLayoutManager(managerDegrees);
        mCourseDegreeList.setHasFixedSize(true);
        mCourseDegreeList.setAdapter(new NumberAdapter(getContext(), sDegrees, R.layout.course_picker_item_degree, this::degreePickerClicked));

        //mCourseNumberList
        LinearLayoutManager managerNumber =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCourseNumberList.setLayoutManager(managerNumber);
        mCourseNumberList.setHasFixedSize(true);
        mCourseNumberList.setAdapter(new NumberAdapter(getContext(), sNumbers, R.layout.course_picker_item_number, this::numberPickerClicked));

        mCourseNumberList.post(this::refreshListSelections);

        mRoot.setVisibility(View.INVISIBLE);
    }

    public void show(long delay) {
        mRoot.setVisibility(View.VISIBLE);

        refreshListSelections();

        mRoot.setAlpha(0);
        mRoot.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

    }

    public void hide(long delay) {
        mRoot.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .withEndAction(() -> mRoot.setVisibility(View.INVISIBLE))
                .start();

    }

    private void degreePickerClicked(String number) {
        long delay = 0;
        mTvDegree.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();
        mTvNumber.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        delay += 100;
        mHandler.postDelayed(() -> {
            if(Integer.parseInt(number) > 10) {
                mTvNumber.setVisibility(View.GONE);
                switchOkCourses(false);
            } else {
                mTvNumber.setVisibility(View.VISIBLE);
                switchOkCourses(true);
            }
            mTvDegree.animate()
                    .setDuration(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .withStartAction(() -> {
                        mTvDegree.setText(number);
                        mTvNumber.setText(Database.courseNumber);
                    })
                    .start();
            mTvNumber.animate()
                    .setDuration(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .start();
        }, delay);

        ((NumberAdapter) mCourseNumberList.getAdapter()).setEnabled(Integer.parseInt(number) <= 10);
    }

    private void numberPickerClicked(String number) {
        long delay = 0;
        mTvNumber.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        delay += 100;
        mHandler.postDelayed(() -> {
            mTvNumber.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(100)
                    .withStartAction(() -> mTvNumber.setText(number))
                    .alpha(1)
                    .start();
        }, delay);
    }

    private void switchOkCourses(boolean toOk) {
        float alphaOk, alphaCourses;
        if(toOk) {
            alphaOk = 1;
            alphaCourses = 0;
        } else {
            alphaOk = 0;
            alphaCourses = 1;
        }
        mBtnOk.animate()
                .setStartDelay(alphaOk == 0 ? 20 : 0)
                .setDuration(80)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(alphaOk)
                .start();
        mBtnCourses.animate()
                .setStartDelay(alphaCourses == 0 ? 20 : 0)
                .setDuration(80)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(alphaCourses)
                .start();
    }

    private void refreshListSelections() {
        ((NumberAdapter) mCourseNumberList.getAdapter()).setNumber(Database.courseNumber);
        ((NumberAdapter) mCourseDegreeList.getAdapter()).setNumber(Database.courseDegree);
    }
}
