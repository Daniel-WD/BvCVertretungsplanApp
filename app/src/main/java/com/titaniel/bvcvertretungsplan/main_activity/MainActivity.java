package com.titaniel.bvcvertretungsplan.main_activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.DateManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.main_activity.course_settings_fragment.CourseSettingsFragment;
import com.titaniel.bvcvertretungsplan.main_activity.day_list.DayListAdapter;
import com.titaniel.bvcvertretungsplan.main_activity.detail_fragment.DetailFragment;
import com.titaniel.bvcvertretungsplan.main_activity.error_fragment.ErrorFragment;
import com.wang.avi.AVLoadingIndicatorView;

import jp.wasabeef.blurry.Blurry;

public class MainActivity extends AppCompatActivity {

    private static final int FM_NONE = 0, FM_DETAIL = 1, FM_COURSE = 2;
    private int mFragmentState = FM_NONE;

    private ImageView mIvBackground;
    private TextView mTvTitle;
    private RecyclerView mRvDays;
    private ImageView mIvLogo;
    private AVLoadingIndicatorView mLoadingView;
    private TextView mTvClass;
    private ImageView mIvEdit;
    private LinearLayout mLyClass;
    private View mVDividerLeft, mVDividerRight;
    private View mVBgOverlay;
    private FrameLayout mBackgroundContainer;

    private DayListAdapter mDayListAdapter;

    private ErrorFragment mErrorFragment;
    private CourseSettingsFragment mCourseSettingsFragment;
    private DetailFragment mDetailFragment;

    private int mClickedIndex = 0;

    private boolean mChangeCourseIvEnabled = true;

    private int mBackgroundDefaultTint = Color.TRANSPARENT, mBackgroundZoomTint = Color.TRANSPARENT;

    private Handler mHandler = new Handler();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        mIvBackground = findViewById(R.id.ivBackground);
        mTvTitle = findViewById(R.id.tvTitle);
        mRvDays = findViewById(R.id.rvDays);
        mTvClass = findViewById(R.id.tvClass);
        mIvLogo = findViewById(R.id.ivLogo);
        mLoadingView = findViewById(R.id.loadingView);
        mIvEdit = findViewById(R.id.ivEdit);
        mLyClass = findViewById(R.id.lyClass);
        mVDividerLeft = findViewById(R.id.vDividerLeft);
        mVDividerRight = findViewById(R.id.vDividerRight);
        mVBgOverlay = findViewById(R.id.vBgOverlay);
        mBackgroundContainer = findViewById(R.id.backgroundContainer);

        //init fragments
        mErrorFragment = (ErrorFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentErr);
        mCourseSettingsFragment = (CourseSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentCourseSettings);
        mDetailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentDetail);

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

        //bg tint colors
        mBackgroundDefaultTint = ContextCompat.getColor(this, R.color.defaultBackgroundTint);
        mBackgroundZoomTint = ContextCompat.getColor(this, R.color.backgroundZoomTint);

        //day list
        GridLayoutManager manager = new GridLayoutManager(this, 16,
                GridLayoutManager.HORIZONTAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) { //how much spans the item needs
                switch(position) {
                    case 0:
                        return 6;
                    case 1:
                        return 6;
                    case 2:
                        return 4;
                    case 3:
                        return 8;
                    case 4:
                        return 8;
                }
                return 8;
            }
        });
        mRvDays.setHasFixedSize(true);
        mRvDays.setLayoutManager(manager);

        //fivEdit
        mIvEdit.setOnClickListener(v -> {
            hideToCourseSettings();
        });

        mHandler.postDelayed(this::startSplash, 100);
    }

    private void hideToDetails(float x, float y, int startRadius) {
        mFragmentState = FM_DETAIL;
        long delay = 0;

        //title
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(0)
                .start();

        //lyClass
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(0)
                .start();

        //ivEdit
        mIvEdit.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //left divider
        mVDividerLeft.setPivotX(mVDividerLeft.getWidth()/2);
        mVDividerLeft.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(0)
                .start();

        //right divider
        mVDividerRight.setPivotX(mVDividerRight.getWidth()/2);
        mVDividerRight.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(0)
                .start();

        //daylistadapter
        mDayListAdapter.hide(delay, mClickedIndex);

        //image zoom
        zoom(delay, 300, 1.1f, false, x, y);

        //background
        mVBgOverlay.setVisibility(View.VISIBLE);
        mVBgOverlay.setAlpha(1);
        float finalRadius = (float) Math.hypot(
                Math.max(x, mVBgOverlay.getWidth() - x),
                Math.max(y, mVBgOverlay.getHeight() - y));

        Animator reveal = ViewAnimationUtils.createCircularReveal(mVBgOverlay, (int) x, (int) y,
                startRadius, finalRadius);
        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
        reveal.setStartDelay(delay);
        reveal.start();

        delay += 100;

        mDetailFragment.show(delay);
    }

    private void showFromDetails(long delay, int index) {
        int realIndex = 0;
        String[] shortDayList = DateManager.shortDayList;
        for(int i = 0; i < shortDayList.length; i++) {
            if(shortDayList[i].equals(DateManager.preparedShortDayList[index])) realIndex = i;
        }

        DayListAdapter.DayHolder holder = ((DayListAdapter.DayHolder)
                mRvDays.findViewHolderForAdapterPosition(DayListAdapter.POSITION_MAPPING_REVERSE[realIndex]));
        int[] loc = holder.location();
        int finalRadius = holder.diameter()/2;
        int x = loc[0] + finalRadius, y = loc[1] + finalRadius;

        //image zoom
        zoom(delay, 300, 1f, false, x, y);

        //background
        float startRadius = (float) Math.hypot(
                Math.max(x, mVBgOverlay.getWidth() - x),
                Math.max(y, mVBgOverlay.getHeight() - y));

        Animator reveal = ViewAnimationUtils.createCircularReveal(mVBgOverlay, x, y,
                startRadius, 0);
        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
        reveal.setStartDelay(delay);
        reveal.setDuration(300);
        reveal.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                mVBgOverlay.setVisibility(View.INVISIBLE);
            }
        });
        reveal.start();

/*        mVBgOverlay.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();*/

        delay += 000;

        //title
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(1)
                .start();

        //lyClass
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(1)
                .start();

        //ivEdit
        mIvEdit.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //left divider
        mVDividerLeft.setPivotX(mVDividerLeft.getWidth()/2);
        mVDividerLeft.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(1)
                .start();

        //right divider
        mVDividerRight.setPivotX(mVDividerRight.getWidth()/2);
        mVDividerRight.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(1)
                .start();

        delay += 300;

        mDayListAdapter.show(delay, realIndex);

    }

    private void hideToCourseSettings() {

        long delay = 0;

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

        //lines
        mVDividerLeft.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();
        mVDividerRight.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //daylist
        mDayListAdapter.hide(delay, -1);

        mCourseSettingsFragment.show(delay);

    }

    private void enterMainComponents(long delay) {
        zoom(delay, 1000, 1f, true);

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

        //left Line
        mVDividerLeft.setVisibility(View.VISIBLE);
        mVDividerLeft.setAlpha(0);
        mVDividerLeft.setTranslationY(50);
        mVDividerLeft.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //right Line
        mVDividerRight.setVisibility(View.VISIBLE);
        mVDividerRight.setAlpha(0);
        mVDividerRight.setTranslationY(50);
        mVDividerRight.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .scaleY(1)
                .alpha(1)
                .translationY(0)
                .start();

        delay += 100;

        //rvDays
        mDayListAdapter.enter(delay);
    }

    private long zoom(long curDelay, long duration, float scale, boolean color) {
        return zoom(curDelay, duration, scale, color, mIvBackground.getWidth()/2, mIvBackground.getHeight()/2);
    }

    private long zoom(long curDelay, long duration, float scale, boolean color, float x, float y) {
        //bg scale in
        mIvBackground.setPivotX(x);
        mIvBackground.setPivotY(y);
        mIvBackground.animate()
                .setStartDelay(curDelay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(duration)
                .scaleX(scale)
                .scaleY(scale)
                .start();
        if(!color) return duration;
        //bg darker
        int cFrom;
        int cTo;
        if(scale != 1) {
            cFrom = mBackgroundDefaultTint;
            cTo = mBackgroundZoomTint;
        } else {
            cTo = mBackgroundDefaultTint;
            cFrom = mBackgroundZoomTint;
        }
        ValueAnimator tintAnim = ValueAnimator.ofArgb(cFrom, cTo);
        tintAnim.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            mIvBackground.setColorFilter(val);
        });
        tintAnim.setStartDelay(curDelay);
        tintAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        tintAnim.setDuration(duration);
        tintAnim.start();

        return duration;
    }

    private void startSplash() {
        long delay = 0;

        zoom(delay, 1300, 1.1f, true);

        delay += 150;

        //draw balls
        mHandler.postDelayed(() -> {
            mIvLogo.setVisibility(View.VISIBLE);
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_logo);
            mIvLogo.setImageDrawable(avd);
            avd.start();
        }, delay);

        mHandler.postDelayed(() -> mIvLogo.setVisibility(View.GONE), 2500);

        delay += 2400;
        startLoading(delay);
    }

    private void startLoading(long delay) {
        //enter loading view

        showLoadingView(delay);
        delay += 500;

        //load data... internet check
        mHandler.postDelayed(() -> {
            boolean isOnline = isOnline();
            if(isOnline) {
                //Database
                Database.fetchData(this, false);
            } else {
                errorOnLoading(ErrorFragment.ERR_NO_INTERNET);
            }

        }, delay);
    }

    private void errorOnLoading(int errorCode) {
        long delay = 0;
        hideLoadingView(0);

        delay += 400;
        mErrorFragment.setError(errorCode);
        mErrorFragment.show(delay);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Database.load();
        updateClassText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Database.save();
    }

    private void updateClassText() {
        if(Integer.parseInt(Database.courseDegree) > 10) {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree));
        } else {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree + "/" + Database.courseNumber));
        }
    }

    public void onDatabaseLoaded(boolean ioException, boolean otherException) {
        if(!ioException && !otherException) {
            Database.loaded = true;
            hideLoadingView(0);

            //set adapter
            mDayListAdapter = new DayListAdapter(this);
            mRvDays.setAdapter(mDayListAdapter);
            mDayListAdapter.setDayListCallback((index, x, y, w) -> {
                mClickedIndex = index;

                int smallIndex = 0;
                String[] preparedShortDayList = DateManager.preparedShortDayList;
                for(int i = 0; i < preparedShortDayList.length; i++) {
                    if(preparedShortDayList[i].equals(DateManager.shortDayList[DayListAdapter.POSITION_MAPPING[index]])) smallIndex = i;
                }

                mDetailFragment.setCurrentIndex(smallIndex);

                hideToDetails(x, y, w/2);
            });

            mHandler.postDelayed(() -> mDetailFragment.onDatabaseLoaded(), 350);

            mRvDays.post(() -> {
                enterMainComponents(1000);
            });

        } else if(ioException) {
            errorOnLoading(ErrorFragment.ERR_IO_EXCEPTION);
        } else {
            errorOnLoading(ErrorFragment.ERR_OTHER_EXCEPTION);
        }
    }

    @Override
    public void onBackPressed() {
        switch(mFragmentState) {
            case FM_COURSE:

                break;
            case FM_DETAIL:
                long delay = 0;
                delay += mDetailFragment.hide(delay);
                showFromDetails(delay, mDetailFragment.getCurrentIndex());
                mFragmentState = FM_NONE;
                break;
            default:
                super.onBackPressed();
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

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
