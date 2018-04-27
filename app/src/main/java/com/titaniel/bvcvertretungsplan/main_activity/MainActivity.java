package com.titaniel.bvcvertretungsplan.main_activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DateManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.Utils;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.main_activity.course_settings_fragment.CourseSettingsFragment;
import com.titaniel.bvcvertretungsplan.main_activity.entry_list_adapter.EntryItemDecoration;
import com.titaniel.bvcvertretungsplan.main_activity.entry_list_adapter.EntryListAdapter;
import com.titaniel.bvcvertretungsplan.main_activity.error_fragment.ErrorFragment;
import com.wang.avi.AVLoadingIndicatorView;

public class MainActivity extends AppCompatActivity {

    private static final int FM_NONE = 0, FM_COURSE = 2;
    private int mFragmentState = FM_NONE;

    private TextView mTvTitle;
    private AVLoadingIndicatorView mLoadingView;
    private TextView mTvClass;
    private ImageView mIvEdit;
    private LinearLayout mLyClass;
    private View mToolbarBottomLine;

    //list
    private RecyclerView mDayList;
    private LinearLayoutManager mLayoutManager;
    private EntryListAdapter mEntryAdapter;

    private ErrorFragment mErrorFragment;
    private CourseSettingsFragment mCourseSettingsFragment;

    private Handler mHandler = new Handler();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        mTvTitle = findViewById(R.id.tvTitle);
        mTvClass = findViewById(R.id.tvClass);
        mLoadingView = findViewById(R.id.loadingView);
        mIvEdit = findViewById(R.id.ivEdit);
        mLyClass = findViewById(R.id.lyClass);
        mDayList = findViewById(R.id.dayList);
        mToolbarBottomLine = findViewById(R.id.toolbarBottomLine);

        //init fragments
        mErrorFragment = (ErrorFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentErr);
        mCourseSettingsFragment = (CourseSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentCourseSettings);

        //errorFragment
        mErrorFragment.setErrorFragmentCallback(new ErrorFragment.ErrorFragmentCallback() {
            @Override
            public void onBtnAgainClicked(Button button) {
                mErrorFragment.hide(0);
                startLoading(300);
            }

            @Override
            public void onBtnOfflineClicked(Button button) {
                mErrorFragment.hide(0);
                Database.fetchData(MainActivity.this, true);
            }
        });

        //Database
        Database.init(this);

        //Day Manager
        DateManager.init(this);

        //entry list
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mDayList.setLayoutManager(mLayoutManager);
//        mDayList.addItemDecoration(new EntryItemDecoration(this));
        mDayList.setHasFixedSize(true);

        //ivEdit
        mIvEdit.setOnClickListener(v -> {
            hideToCourseSettings();
        });

        mHandler.postDelayed(() -> startLoading(0), 100);
    }

    private void startLoading(long delay) {
        //enter loading view

        showLoadingView(delay);
        delay += 500;

        //load data... internet check
        mHandler.postDelayed(() -> {
            boolean isOnline = Utils.isOnline(this);
            if(isOnline) {
                //Database
                Database.fetchData(this, false);
            } else {
                errorOnLoading(ErrorFragment.ERR_NO_INTERNET);
            }

        }, delay);
    }

    public void onDatabaseLoaded(boolean ioException, boolean otherException) {
        if(!ioException && !otherException) {
            Database.loaded = true;
            hideLoadingView(0);

            fillList();

            mDayList.post(() -> {
                enterMainComponents(1000);
            });

        } else if(ioException) {
            errorOnLoading(ErrorFragment.ERR_IO_EXCEPTION);
        } else {
            errorOnLoading(ErrorFragment.ERR_OTHER_EXCEPTION);
        }
    }

    public void fillList() {
        mEntryAdapter = new EntryListAdapter(this);
        mDayList.setAdapter(mEntryAdapter);
    }

    private void errorOnLoading(int errorCode) {
        long delay = 0;
        hideLoadingView(0);

        delay += 400;
        mErrorFragment.setError(errorCode);
        mErrorFragment.show(delay);
    }

    private void enterMainComponents(long delay) {
        //title
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setAlpha(0);
        mTvTitle.setTranslationY(50);
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .scaleY(1)
                .scaleX(1)
                .translationY(0)
                .start();

        delay += 50;

        //class layout
        mLyClass.setVisibility(View.VISIBLE);
        mLyClass.setAlpha(0);
        mLyClass.setTranslationY(50);
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        delay += 50;

        //ivEdit
        mIvEdit.setVisibility(View.VISIBLE);
        mIvEdit.setAlpha(0f);
        mIvEdit.setTranslationY(50);
        mIvEdit.setRotation(-90);
        mIvEdit.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .alpha(1)
                .translationY(0)
                .scaleX(1)
                .scaleY(1)
                .rotation(0)
                .start();

        delay += 50;
    }

    private void hideToCourseSettings() {
        long delay = 0;

        mFragmentState = FM_COURSE;

        //title
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //class layout
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //edit imageview
        mIvEdit.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //edit imageview
        mDayList.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //edit imageview
        mToolbarBottomLine.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        mCourseSettingsFragment.show(delay);
    }

    private void showFromCourseSettings() {
        long delay = 0;

        mFragmentState = FM_NONE;

        updateClassText();

        //title
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //class layout
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //edit imageview
        mIvEdit.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //edit imageview
        mDayList.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //edit imageview
        mToolbarBottomLine.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        mCourseSettingsFragment.hide(delay);
    }

    private void updateClassText() {
        if(Integer.parseInt(Database.courseDegree) > 10) {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree));
        } else {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree + "/" + Database.courseNumber));
        }
    }

    private void showLoadingView(long delay) {
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setAlpha(0);
        mLoadingView.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300)
                .alpha(1)
                .start();
    }

    private void hideLoadingView(long delay) {
        mLoadingView.setAlpha(1);
        mLoadingView.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(300)
                .alpha(0)
                .withEndAction(() -> {
                    mLoadingView.setVisibility(View.GONE);
                })
                .start();
    }

    @Override
    public void onBackPressed() {
        switch(mFragmentState) {
            case FM_COURSE:
                showFromCourseSettings();
                break;
            default:
                super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hide statusbar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        //load database
        Database.load();
        updateClassText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Database.save();
    }
}
