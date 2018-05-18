package com.titaniel.bvcvertretungsplan.main_activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.authentication.AuthManager;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.main_activity.course_settings.CourseSettingsFragment;
import com.titaniel.bvcvertretungsplan.main_activity.day_list.DayListAdapter;
import com.titaniel.bvcvertretungsplan.main_activity.error_fragment.ErrorFragment;
import com.titaniel.bvcvertretungsplan.main_activity.login_fragment.LoginFragment;
import com.titaniel.bvcvertretungsplan.utils.DateManager;
import com.titaniel.bvcvertretungsplan.utils.Utils;
import com.victor.loading.rotate.RotateLoading;

public class MainActivity extends AppCompatActivity {

    private static final int FM_NONE = 0, FM_COURSE = 1, FM_LOGIN = 2, FM_ERROR = 3;
    private int mFragmentState = FM_NONE;

    private Runnable mRBringDayListToTop = new Runnable() {

        boolean wasNotIdle = false;

        @Override
        public void run() {
            if(mDayList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && wasNotIdle) {
//                mDayList.scrollTo(0, 0);
                mDayList.scrollToPosition(0);

                mHandler.postDelayed(() -> {
//                    View child = mDayList.getChildAt(0);
//                    float y = child.getY();
//                    mDayList.scrollBy(0, (int) -(mFixedFirstItemPosition - y));
                    if(mFragmentState == FM_NONE) {
                        enterMainComponents(0);
                    } else {
                        onBackPressed();
                    }
                }, 10);
            } else {
                wasNotIdle = true;
                mHandler.post(mRBringDayListToTop);
            }
        }
    };

    private boolean mHeaderFadeEnabled = false;
    private boolean mHeaderVisible = true;
    private static boolean sIsPaused = false;

    private ImageView mIvBackground;
    private TextView mTvTitle;
    private RotateLoading mLoadingView;
    private ImageView mIvEdit;
    private View mVBgOverlay;
    private LinearLayout mLyClass;
    private TextView mTvClass;
    private TextView mTvNothing;

    //list
    private RecyclerView mDayList;
    private LinearLayoutManager mLayoutManager;
    private DayListAdapter mDayListAdapter;

    private ErrorFragment mErrorFragment;
    private CourseSettingsFragment mCourseSettingsFragment;
    private LoginFragment mLoginFragment;

    private float mFixedFirstItemPosition;

    private Handler mHandler = new Handler();

    private int mLoadingOverlay, mHeaderHiddenOverlay;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bg colors
        mLoadingOverlay = ContextCompat.getColor(this, R.color.backgroundOverlayLoading);
        mHeaderHiddenOverlay = ContextCompat.getColor(this, R.color.backgroundOverlayHeaderHidden);

        //init views
        mTvTitle = findViewById(R.id.tvTitle);
        mLoadingView = findViewById(R.id.loadingView);
        mIvEdit = findViewById(R.id.ivEdit);
        mDayList = findViewById(R.id.dayList);
        mIvBackground = findViewById(R.id.ivBackground);
        mVBgOverlay = findViewById(R.id.vBackgroundOverlay);
        mLyClass = findViewById(R.id.lyClass);
        mTvClass = findViewById(R.id.tvClass);
        mTvNothing = findViewById(R.id.tvNothing);

        mLoadingView.start();

        //init fragments
        mErrorFragment = (ErrorFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentErr);
        mCourseSettingsFragment = (CourseSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentCourseSettings);
        mLoginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentLogin);

        //errorFragment
        mErrorFragment.setErrorFragmentCallback(new ErrorFragment.ErrorFragmentCallback() {
            @Override
            public void onBtnAgainClicked(Button button) {
                mErrorFragment.hide(0);
                login(300);
                mFragmentState = FM_NONE;
            }

            @Override
            public void onBtnOfflineClicked(Button button) {
                mErrorFragment.hide(0);
                Database.fetchData(MainActivity.this, true);
                mFragmentState = FM_NONE;
            }
        });

        //Database
        Database.init(this);

        //Day Manager
        DateManager.init(this);

        //day list
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mDayList.setLayoutManager(mLayoutManager);
        mDayList.setHasFixedSize(true);
        mDayList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                View child = mDayList.getChildAt(0);
                if(recyclerView.getChildAdapterPosition(child) == 0) {
                    float y = child.getY();
                    onScroll(mFixedFirstItemPosition - y, y);
                    Log.d("TAG", " ---- " + (mFixedFirstItemPosition - y));
                }
                super.onScrolled(recyclerView, dx, dy);
            }

        });

        //ivEdit
        mIvEdit.setOnClickListener(v -> {
            if(!mHeaderVisible) return;
            if(mFragmentState == FM_NONE) {
                hideToCourseSettings();
            } else {
                long delay = mCourseSettingsFragment.hide(0);
                showFromCourseSettings(delay);
            }
        });

        //loading view
        mLoadingView.setAlpha(0);

        makeInvisible();

        mHandler.postDelayed(() -> login(0), 100);
    }

    private void makeInvisible() {
        mTvTitle.setVisibility(View.INVISIBLE);
        mIvEdit.setVisibility(View.INVISIBLE);
        mLyClass.setVisibility(View.INVISIBLE);
        mDayList.setVisibility(View.INVISIBLE);
    }

    private void onScroll(float distance, float y) {
        if(!mHeaderFadeEnabled) return;
        if(distance == 0) {
            showHeader();
        } else {
            hideHeader();
        }
    }

    private void hideHeader() {
        if(!mHeaderVisible) return;
        mHeaderVisible = false;
        long delay = 0;

/*        //overlay
        mVBgOverlay.setBackgroundColor(mHeaderHiddenOverlay);
        mVBgOverlay.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();*/

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
        mTvTitle.animate()
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

    private void showHeader() {
        if(mHeaderVisible) return;
        mHeaderVisible = true;
        long delay = 0;

/*        //overlay
        mVBgOverlay.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();*/

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
        mTvTitle.animate()
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

    private void login(long delay) {
        showLoadingView(delay);
        delay += 300;

        //login and internet check
        mHandler.postDelayed(() -> {
            boolean isOnline = Utils.isOnline(this);
            if(isOnline) {
                //Database
                AuthManager.checkLogin(success -> {
                    if(success) {
                        startLoading(0);
                    } else {
                        if(Utils.isOnline(this)) {
                            hideLoadingView(0);
                            mFragmentState = FM_LOGIN;
                            mLoginFragment.show(300);
                        } else {
                            errorOnLoading(ErrorFragment.ERR_NO_INTERNET);
                        }
                    }
                });
            } else {
                errorOnLoading(ErrorFragment.ERR_NO_INTERNET);
            }

        }, delay);

    }

    public void startLoading(long delay) {
        mFragmentState = FM_NONE;

        if(!Database.courseChosen) {
            mHandler.postDelayed(() -> {
                mIvEdit.callOnClick();
            }, delay);
            return;
        }

        //enter loading view
        showLoadingView(delay);
        delay += 300;

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

    public void onDatabaseLoaded(boolean ioException, boolean otherException, boolean internetCut) {
        if(!ioException && !otherException && !internetCut) {
            Database.loaded = true;
            hideLoadingView(0);

            mHandler.postDelayed(() -> {
                fillList(() -> enterMainComponents(0));
            }, 300);

        } else if(ioException) {
            errorOnLoading(ErrorFragment.ERR_IO_EXCEPTION);
        } else if(otherException) {
            errorOnLoading(ErrorFragment.ERR_OTHER_EXCEPTION);
        } else {
            errorOnLoading(ErrorFragment.ERR_NO_INTERNET);
        }
    }

    public void fillList(Runnable runnable) {
        mHeaderFadeEnabled = false;
        mDayListAdapter = new DayListAdapter(this);
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

    private void errorOnLoading(int errorCode) {
        long delay = 0;
        hideLoadingView(0);

        delay += 400;
        mErrorFragment.setError(errorCode);
        mFragmentState = FM_ERROR;
        mErrorFragment.show(delay);
    }

    private void enterMainComponents(long delay) {
        updateClassText();

        //Background overlay
        mVBgOverlay.animate()
                .setStartDelay(delay)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //ivEdit
        mIvEdit.setImageResource(R.drawable.ic_filter_variant);
        mIvEdit.setVisibility(View.VISIBLE);
        mIvEdit.setAlpha(0f);
        mIvEdit.setTranslationY(50);
        mIvEdit.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .translationY(0)
                .alpha(1)
                .start();

        //title
        delay += 50;
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setAlpha(0f);
        mTvTitle.setTranslationY(50);
        mTvTitle.animate()
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
        mLyClass.setAlpha(0f);
        mLyClass.setTranslationY(50);
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
            mTvNothing.setVisibility(View.VISIBLE);
            mTvNothing.setAlpha(0f);
            mTvNothing.setTranslationY(50);
            mTvNothing.animate()
                    .setStartDelay(delay)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .translationY(0)
                    .alpha(1)
                    .start();
        } else {
            mTvNothing.setVisibility(View.INVISIBLE);
        }

        //list
        delay += 50;
        mDayList.setVisibility(View.VISIBLE);
        mDayListAdapter.show(delay);
    }

    private void hideToCourseSettings() {
        long delay = 0;

        mFragmentState = FM_COURSE;

        //avd animation
        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.avd_filter_to_close, getTheme());
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
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //tv nothing
        if(mTvNothing.getVisibility() == View.VISIBLE) {
            mTvNothing.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(0)
                    .start();
        }

        //daylist
        if(mDayListAdapter != null) mDayListAdapter.hide(delay);

        delay += 200;

        mCourseSettingsFragment.show(delay);
    }

    private void showFromCourseSettings(long delay) {

        mFragmentState = FM_NONE;
        updateClassText();

        //avd animation
        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.avd_close_to_filter, getTheme());
        mIvEdit.setImageDrawable(drawable);
        drawable.start();

        //title
        delay += 50;
        mTvTitle.setTranslationY(50);
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .translationY(0)
                .withEndAction(() -> mHeaderFadeEnabled = true)
                .alpha(1)
                .start();

        //class ly
        delay += 50;
        mLyClass.setTranslationY(50);
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
            mTvNothing.setVisibility(View.VISIBLE);
            mTvNothing.setTranslationY(50);
            mTvNothing.setAlpha(0);
            mTvNothing.animate()
                    .setStartDelay(delay)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .translationY(0)
                    .alpha(1)
                    .start();
        }

        //list
        delay += 50;
        if(mDayListAdapter != null) mDayListAdapter.show(delay);
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
        mLoadingView.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300)
                .alpha(1)
                .start();
    }

    private void hideLoadingView(long delay) {
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
                if(!Database.courseChosen) {
                    super.onBackPressed();
                    return;
                }
                long delay = mCourseSettingsFragment.hide(0);
                showFromCourseSettings(delay);
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

        mHandler.postDelayed(() -> {
            if(mDayList.getChildCount() != 0) mFixedFirstItemPosition = mDayList.getChildAt(0).getY();
            switch(mFragmentState) {
                case FM_COURSE:
                    mCourseSettingsFragment.show(0);
                    break;
                case FM_LOGIN:
                    mLoginFragment.show(0);
                    break;
                case FM_ERROR:
                    mErrorFragment.show(0);
                    break;
                case FM_NONE:
                    if(mDayList.getChildCount() == 0) {
                        mTvNothing.setAlpha(1);
                    } else {
                        mTvNothing.setAlpha(0);
                    }
                    //ly class
                    mLyClass.setTranslationY(0);
                    mLyClass.setAlpha(1);

                    //title
                    mTvTitle.setTranslationY(0);
                    mTvTitle.setAlpha(1);

                    //edit
                    mIvEdit.setTranslationY(0);
                    mIvEdit.setAlpha(1f);
            }
        }, 500);

        //load database
        Database.load();

        sIsPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sIsPaused = true;
        Database.save();
    }
}
