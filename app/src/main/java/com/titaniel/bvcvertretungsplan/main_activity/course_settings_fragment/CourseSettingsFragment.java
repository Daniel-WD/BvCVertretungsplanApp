package com.titaniel.bvcvertretungsplan.main_activity.course_settings_fragment;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;

public class CourseSettingsFragment extends Fragment {

    private static final String[] sDegrees = {"5", "6", "7", "8", "9", "10", "11", "12"};
    private static final String[] sNumbers = {"1", "2", "3", "4", "5", "6"};

    private View mRoot;
    private RecyclerView mCourseDegreeList, mCourseNumberList;
    private TextView mTvCourseDegree, mTvCourseNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_settings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //init
        mRoot = getView();
        mCourseDegreeList = mRoot.findViewById(R.id.courseDegreeList);
        mCourseNumberList = mRoot.findViewById(R.id.courseNumberList);
        mTvCourseDegree = mRoot.findViewById(R.id.tvCourseDegree);
        mTvCourseNumber = mRoot.findViewById(R.id.tvCourseNumber);

        //mCourseDegreeList
        LinearLayoutManager managerDegrees =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCourseDegreeList.setLayoutManager(managerDegrees);
        mCourseDegreeList.setHasFixedSize(true);
        int colorFour = ContextCompat.getColor(getContext(), R.color.four);
        mCourseDegreeList.setAdapter(
                new NumberAdapter(mCourseDegreeList, sDegrees, getContext(), colorFour, R.layout.course_picker_item_degree, number -> {

                    if(!mTvCourseDegree.getText().toString().equals(number)) {
                        Database.courseDegree = number;
                        mTvCourseNumber.animate()
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .setDuration(100)
                                .alpha(0)
                                .start();
                        mTvCourseDegree.animate()
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .setDuration(100)
                                .alpha(0)
                                .withEndAction(() -> {
                                    if(Integer.parseInt(number) > 10) {
                                        mTvCourseNumber.setVisibility(View.GONE);
                                    } else {
                                        mTvCourseNumber.setVisibility(View.VISIBLE);
                                    }
                                    mTvCourseDegree.setText(number);
                                    mTvCourseDegree.animate()
                                            .setInterpolator(new AccelerateDecelerateInterpolator())
                                            .setDuration(100)
                                            .alpha(1)
                                            .start();
                                    mTvCourseNumber.setText(Database.courseNumber);//prevent issues on start
                                    mTvCourseNumber.animate()
                                            .setInterpolator(new AccelerateDecelerateInterpolator())
                                            .setDuration(100)
                                            .alpha(1)
                                            .start();
                                })
                                .start();
                    }

                    if(Integer.parseInt(number) > 10) {
                        ((NumberAdapter) mCourseNumberList.getAdapter()).setEnabled(false);
                        mCourseNumberList.animate()
                                .setInterpolator(new FastOutSlowInInterpolator())
                                .setDuration(200)
                                .translationY(10)
                                .alpha(0.5f)
                                .start();
                    } else {
                        ((NumberAdapter) mCourseNumberList.getAdapter()).setEnabled(true);
                        mCourseNumberList.animate()
                                .setInterpolator(new FastOutSlowInInterpolator())
                                .setDuration(200)
                                .translationY(0)
                                .alpha(1)
                                .start();
                    }
                }));
        //mCourseNumberList
        LinearLayoutManager managerNumber =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCourseNumberList.setLayoutManager(managerNumber);
        int colorFourDark = ContextCompat.getColor(getContext(), R.color.four_dark);
        mCourseNumberList.setAdapter(
                new NumberAdapter(mCourseNumberList, sNumbers, getContext(), colorFourDark, R.layout.course_picker_item_number, number -> {
                    if(!mTvCourseNumber.getText().toString().equals(number)) {
                        Database.courseNumber = number;
                        mTvCourseNumber.animate()
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .setDuration(100)
                                .alpha(0)
                                .withEndAction(() -> {
                                    mTvCourseNumber.setText(number);
                                    mTvCourseNumber.animate()
                                            .setInterpolator(new AccelerateDecelerateInterpolator())
                                            .setDuration(100)
                                            .alpha(1)
                                            .start();
                                })
                                .start();
                    }
                }));
        mCourseNumberList.setHasFixedSize(true);

        mRoot.setVisibility(View.INVISIBLE);
    }

    public void show(long delay) {
        mRoot.setVisibility(View.VISIBLE);

        mRoot.setAlpha(0);
        mRoot.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

    }

    private void refreshListSelections() {
        ((NumberAdapter) mCourseNumberList.getAdapter()).setNumber(Database.courseNumber);
        ((NumberAdapter) mCourseDegreeList.getAdapter()).setNumber(Database.courseDegree);
    }
}
