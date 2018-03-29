package com.titaniel.bvcvertretungsplan;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.course_picker.NumberAdapter;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.day_indicator_list.DayListAdapter;
import com.titaniel.bvcvertretungsplan.day_pager.DayPagerAdapter;
import com.titaniel.bvcvertretungsplan.day_pager.DayPagerTransformer;
import com.titaniel.bvcvertretungsplan.main_pager.MainPagerAdapter;
import com.titaniel.bvcvertretungsplan.my_viewpager.MyViewPager;

import java.util.Random;

import jp.wasabeef.blurry.Blurry;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class MainActivity extends AppCompatActivity {

    private static final String[] sDegrees = {"5", "6", "7", "8", "9", "10", "11", "12"};
    private static final String[] sNumbers = {"1", "2", "3", "4", "5", "6"};

    private static final float HEADER_FADE_SPEED = 3f;

    private static final int ERR_NO_INTERNET = 0;
    private static final int ERR_IO_EXCEPTION = 1;
    private static final int ERR_OTHER_EXCEPTION = 2;

    private RecyclerView mDayIndicator;
    private MyViewPager mDayPager;
    private ConstraintLayout mHeaderContainer;
    private AppBarLayout mAppBarLayout;
    private TextView mTvClass;
    private FrameLayout mListBackground;
    private ImageView mIvBackground;
    private ImageView mIvLogo;
    private View mLoadingView;
    private ImageView mIvErr;
    private LinearLayout mErrLayout;
    private TextView mTvErr;
    private Button mBtnErrAgain, mBtnErrOffline;
    private LinearLayout mCoursePickerLayout;
    private RecyclerView mCourseDegreeList, mCourseNumberList;
    private TextView mTvCourseDegree, mTvCourseNumber;
    private MyViewPager mMainPager;
    private FrameLayout mBgContainer;
    private ImageView mIvChangeCourse;
    private TextView mTvTitle;

    private View mCurrentDayPagerDay, mCurrentDayPagerNumber, mCurrentDayPagerMonth, mCurrentMainPagerView;

    private boolean mChangeCourseIvEnabled = true;
    private boolean mCollapsingEnabled = true;
    private boolean mStarted = false;

    private int mWidth, mHeight;

    private AppBarLayout.Behavior mAppBarBehavior;
    private AppBarLayout.OnOffsetChangedListener mAppBarOffsetChangedListener;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Database
        Database.init(this);

        //Day Manager
        DayManager.init(this);

        //Size
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        //Init
        mDayIndicator = findViewById(R.id.listDays);
        mDayPager = findViewById(R.id.dayPager);
        mAppBarLayout = findViewById(R.id.appbarlayout);
        mHeaderContainer = findViewById(R.id.headerContainer);
        mTvClass = findViewById(R.id.tvClass);
        mListBackground = findViewById(R.id.listBackground);
        mIvBackground = findViewById(R.id.background);
        mIvLogo = findViewById(R.id.ivLogo);
        mLoadingView = findViewById(R.id.loadingView);
        mIvErr = findViewById(R.id.ivErr);
        mErrLayout = findViewById(R.id.errorLayout);
        mTvErr = findViewById(R.id.tvErr);
        mBtnErrAgain = findViewById(R.id.btnErrAgain);
        mBtnErrOffline = findViewById(R.id.btnErrOffline);
        mCoursePickerLayout = findViewById(R.id.coursePickerLayout);
        mCourseDegreeList = findViewById(R.id.courseDegreeList);
        mCourseNumberList = findViewById(R.id.courseNumberList);
        mTvCourseDegree = findViewById(R.id.tvCourseDegree);
        mTvCourseNumber = findViewById(R.id.tvCourseNumber);
        mMainPager = findViewById(R.id.mainPager);
        mBgContainer = findViewById(R.id.backgroundContainer);
        mIvChangeCourse = findViewById(R.id.ivChangeCourse);
        mTvTitle = findViewById(R.id.tvTitle);

/*        //background blurr
        mRoundShadow.post(() -> {
            //Blurry.with(MainActivity.this).capture(mRound).radius(10).sampling(2).(mBgContainer);
            //Blurry.with(MainActivity.this).radius(25).sampling(1).capture(mRoundShadow).into(mRoundShadow);
        });*/

        //mainPager
        OverScrollDecoratorHelper.setUpOverScroll(mMainPager);
        mMainPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        mMainPager.setOffscreenPageLimit(5);
        mMainPager.post(this::updateCurrentMainPagerView);
        mMainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int oldPos = 0;

            @Override
            public void onPageScrollStateChanged(int state) {
                int newPos = mMainPager.getCurrentItem();
                switch(state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        mChangeCourseIvEnabled = true;
                        mCollapsingEnabled = true;
                        if(newPos != oldPos) {
                            ((MainPagerAdapter) mMainPager.getAdapter()).getEntryList(oldPos).scrollToPosition(0);
                        }
                        oldPos = newPos;
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mChangeCourseIvEnabled = false;
                        mCollapsingEnabled = false;
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        mChangeCourseIvEnabled = false;
                        mCollapsingEnabled = true;
                        mDayPager.setCurrentItem(newPos, true);
                        updateScrollEnabledForPos(newPos);
                        break;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCollapsingEnabled = true;
//                ValueAnimator anim = ValueAnimator.ofInt(mAppBarBehavior.getTopAndBottomOffset(), 0);
//                anim.addUpdateListener(animation -> {
//                    int v = (int) animation.getAnimatedValue();
//                    //mAppBarBehavior.setTopAndBottomOffset(v);
//                    //mAppBarLayout.invalidate();
//                    //mAppBarOffsetChangedListener.onOffsetChanged(mAppBarLayout, v);
//                });
//                anim.setInterpolator(new FastOutSlowInInterpolator());
//                anim.setDuration(100);
//                anim.start();
                if(mHeight - mAppBarLayout.getHeight() >
                        ((MainPagerAdapter) mMainPager.getAdapter()).getEntryList(position).getHeight()
                        || mAppBarLayout.getTop() >= -0.1*mAppBarLayout.getHeight()) {
                    mAppBarLayout.setExpanded(true, true);
                }

                updateCurrentMainPagerView();
            }
        });

        //daypager
        mDayPager.setAdapter(new DayPagerAdapter(getSupportFragmentManager()));
        mDayPager.setOffscreenPageLimit(5);
        mDayPager.post(() -> {
            DayPagerTransformer transformer = new DayPagerTransformer();
            mDayPager.setPageTransformer(false, transformer);

            //adjust date container position
            updateCurrentDayPagerViews();

            float m = getResources().getDimensionPixelSize(R.dimen.dateMarginTop);
            float y = mCurrentDayPagerMonth.getY() + mCurrentDayPagerMonth.getHeight() + m;
            //mDayIndicator.setY(y);
        });
        mDayPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mMainPager.setCurrentItem(position, true);
                ((DayListAdapter) mDayIndicator.getAdapter()).changeSelected(position);
                updateCurrentDayPagerViews();
            }
        });

        //mCourseDegreeList
        LinearLayoutManager managerDegrees =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCourseDegreeList.setLayoutManager(managerDegrees);
        mCourseDegreeList.setHasFixedSize(true);
        int colorFour = ContextCompat.getColor(this, R.color.four);
        mCourseDegreeList.setAdapter(
                new NumberAdapter(mCourseDegreeList, sDegrees, this, colorFour, R.layout.list_item_degree, number -> {

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
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCourseNumberList.setLayoutManager(managerNumber);
        int colorFourDark = ContextCompat.getColor(this, R.color.four_dark);
        mCourseNumberList.setAdapter(
                new NumberAdapter(mCourseNumberList, sNumbers, this, colorFourDark, R.layout.list_item_number, number -> {
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

        //fab
        mIvChangeCourse.setOnClickListener(v -> {
            if(!mChangeCourseIvEnabled || mAppBarBehavior.getTopAndBottomOffset() != 0) return;
            mChangeCourseIvEnabled = false;
            if(mCoursePickerLayout.getVisibility() == View.INVISIBLE) {
                showCoursePicker();

                //avd
                AnimatedVectorDrawable sortToCross = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_sort_to_cross, getTheme());
                mIvChangeCourse.setImageDrawable(sortToCross);
                sortToCross.start();
            } else {
                refreshMainPager();
                mHandler.postDelayed(() -> {
                    markIndicators();
                    hideCoursePicker();
                    updateScrollEnabledForPos(mMainPager.getCurrentItem());

                    //avd
                    AnimatedVectorDrawable crossToSort = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_cross_to_sort, getTheme());
                    mIvChangeCourse.setImageDrawable(crossToSort);
                    crossToSort.start();
                }, 50);
            }
        });

        //btn err again
        mBtnErrAgain.setOnClickListener(v -> {
            mErrLayout.animate()
                    .setStartDelay(0)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(200)
                    .alpha(0)
                    .translationY(100)
                    .withEndAction(() -> mErrLayout.setVisibility(View.INVISIBLE))
                    .start();

            startLoading(200);
        });

        //btn err offline
        mBtnErrOffline.setOnClickListener(v -> {
            mErrLayout.animate()
                    .setStartDelay(0)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(200)
                    .alpha(0)
                    .translationY(100)
                    .withEndAction(() -> {
                        mErrLayout.setVisibility(View.INVISIBLE);
                        Database.fetchData(MainActivity.this, true);
                        //too short xD
//                        mLoadingView.animate()
//                                .setInterpolator(new AccelerateDecelerateInterpolator())
//                                .setStartDelay(0)
//                                .setDuration(500)
//                                .alpha(1)
//                                .start();
                        //enterMainComponents(300);
                    })
                    .start();
        });

        //AppBarLayout
        //wait to prevent fab position
        mAppBarLayout.postDelayed(() -> {
            mAppBarLayout.addOnOffsetChangedListener(
                    mAppBarOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {

                        boolean flag = true;
                        float fabTY = 0;

                        @Override
                        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                            if(!mCollapsingEnabled || !mStarted) return;
                            if(verticalOffset < 0) {
                                mDayPager.setEnabled(false);
                            } else {
                                mDayPager.setEnabled(true);
                            }
                            mHeaderContainer.setAlpha(1 - ((float) Math.abs(verticalOffset)*HEADER_FADE_SPEED)/(float) appBarLayout.getHeight());
                        }
                    });
        }, 100);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        mAppBarBehavior = new AppBarLayout.Behavior();
        mAppBarBehavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            boolean flag = true;
            float y;

            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
                /*if(flag) {
                    y = mAppBarLayout.getBottom();
                    flag = false;
                }

                RecyclerView list = ((MainPagerAdapter)
                        mMainPager.getAdapter()).getEntryList(mMainPager.getCurrentItem());
                return list.getChildCount() != 0 && mCollapsingEnabled && mHeight - y < list.getHeight();*/
            }
        });
        params.setBehavior(mAppBarBehavior);

        //days indicator
        LinearLayoutManager managerDays =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mDayIndicator.setLayoutManager(managerDays);
        mDayIndicator.setAdapter(new DayListAdapter(mDayIndicator, mDayPager));
        mDayIndicator.setHasFixedSize(true);

        hideUi();

        mHandler.postDelayed(() -> {
            startSplash();
        }, 400);
    }

    private void updateCurrentDayPagerViews() {
        mCurrentDayPagerDay = mDayPager.getChildAt(mDayPager.getCurrentItem()).findViewById(R.id.dayText);
        mCurrentDayPagerMonth = mDayPager.getChildAt(mDayPager.getCurrentItem()).findViewById(R.id.tvMonthYear);
        mCurrentDayPagerNumber = mDayPager.getChildAt(mDayPager.getCurrentItem()).findViewById(R.id.tvDayInMonth);
    }

    private void updateCurrentMainPagerView() {
        mCurrentMainPagerView = mMainPager.getChildAt(mMainPager.getCurrentItem());
    }

    private void updateScrollEnabledForPos(int pos) {
        MainPagerAdapter adapter = ((MainPagerAdapter) mMainPager.getAdapter());
        if(mHeight - mAppBarLayout.getHeight() > adapter.getEntryList(pos).getHeight() || adapter.getEntryList(pos).getChildCount() == 0) {
            mAppBarLayout.setExpanded(true, true);
            adapter.getFragment(pos).scrollEnabled = false;
        } else {
            adapter.getFragment(pos).scrollEnabled = true;
        }
    }

    private void updateClassText() {
        if(Integer.parseInt(Database.courseDegree) > 10) {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree));
        } else {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree + "/" + Database.courseNumber));
        }
    }

    private void showCoursePicker() {
        mMainPager.setEnabled(false);
        mDayPager.setEnabled(false);
        mChangeCourseIvEnabled = false;
        mHandler.postDelayed(() -> mChangeCourseIvEnabled = true, 350/*zoomtime*/);
        mCollapsingEnabled = false;

        ((MainPagerAdapter) mMainPager.getAdapter()).setScrollEnabled(false);
        long delay = 0;
        float ty;

        //zoom
        //zoom(delay, 300, true);

        long headerOutDur = 100;

        //header out
        ty = -50;
        mTvTitle.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(headerOutDur)
                .translationYBy(ty)
                .alpha(0)
                .start();
        mTvClass.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(headerOutDur)
                .translationYBy(ty)
                .alpha(0)
                .start();

        delay += 30;
        mCurrentDayPagerDay.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(headerOutDur)
                .translationYBy(ty)
                .alpha(0)
                .start();
        delay += 30;
        mCurrentDayPagerNumber.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(headerOutDur)
                .translationYBy(ty)
                .alpha(0)
                .start();
        delay += 30;
        mCurrentDayPagerMonth.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(headerOutDur)
                .translationYBy(ty)
                .alpha(0)
                .start();

        delay += 30;
        mDayIndicator.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(headerOutDur)
                .translationYBy(ty)
                .alpha(0)
                .start();

        delay += 150;

        long moveDur = 300;
//        ty = mCoursePickerLayout.getY() - mCoursePickerLayout.getTranslationY() - mRound.getY() + mRound.getHeight()/2;
        // TODO: 25.03.2018 hello
        ty = 100;

        //show picker
        mCoursePickerLayout.setVisibility(View.VISIBLE);
        mCoursePickerLayout.setAlpha(0f);
        mCoursePickerLayout.setTranslationY(-ty);
        mCoursePickerLayout.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(moveDur)
                .alpha(1)
                .translationY(0)
                .withEndAction(() -> mChangeCourseIvEnabled = true)
                .start();

        mListBackground.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(ty)
                .start();
        mCurrentMainPagerView.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(ty)
                .alpha(0f)
                .start();

    }

    private void hideCoursePicker() {
        mChangeCourseIvEnabled = false;
        mHandler.postDelayed(() -> mChangeCourseIvEnabled = true, 300/*zoomtime*/);
        ((MainPagerAdapter) mMainPager.getAdapter()).setScrollEnabled(true);

        updateClassText();

        long delay = 0;
        //move bottom sheet in
        long moveDur = 300;

        //zoom
        //zoom(delay, 300, false);

//        float ty = mFakeCut.getY() - mRealCut.getY();
        // TODO: 25.03.2018 hello
        float ty = 100;


        //hide picker
        mCoursePickerLayout.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(100)
                .withEndAction(() -> {
                    mCoursePickerLayout.setVisibility(View.INVISIBLE);
                })
                .alpha(0)
                .translationY(-50)
                .start();

        delay += 50;

        mListBackground.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(-ty)
                .start();
        mCurrentMainPagerView.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(-ty)
                .alpha(1f)
                .withEndAction(() -> {
                    mCollapsingEnabled = true;
                    mChangeCourseIvEnabled = true;
                    mMainPager.setEnabled(true);
                    mDayPager.setEnabled(true);
                })
                .start();

        delay += 150;

        //header int
        long headerInDur = 150;

        ty = 100;

        mDayIndicator.setTranslationY(mDayIndicator.getTranslationY() - (ty - 50));
        mCurrentDayPagerDay.setTranslationY(mCurrentDayPagerDay.getTranslationY() - (ty - 50));
        mCurrentDayPagerNumber.setTranslationY(mCurrentDayPagerNumber.getTranslationY() - (ty - 50));
        mCurrentDayPagerMonth.setTranslationY(mCurrentDayPagerMonth.getTranslationY() - (ty - 50));
        mTvTitle.setTranslationY(mTvTitle.getTranslationY() - (ty - 50));
        mTvClass.setTranslationY(mTvClass.getTranslationY() - (ty - 50));

        mDayIndicator.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();
        delay += 30;
        mCurrentDayPagerDay.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();
        delay += 30;
        mCurrentDayPagerNumber.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();
        delay += 30;
        mCurrentDayPagerMonth.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();
        delay += 30;
        mTvTitle.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();
        mTvClass.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();

    }

    private void hideUi() {
        mMainPager.setVisibility(View.INVISIBLE);
        mListBackground.setVisibility(View.INVISIBLE);
        mTvTitle.setVisibility(View.INVISIBLE);
        mTvClass.setVisibility(View.INVISIBLE);
        mIvChangeCourse.setVisibility(View.INVISIBLE);
        mDayPager.setVisibility(View.INVISIBLE);
        mDayIndicator.setVisibility(View.INVISIBLE);
    }

    private void initStartAnimState() {
        float ty = 300;

        mListBackground.setTranslationY(mListBackground.getTranslationY() + ty);
        mListBackground.setVisibility(View.VISIBLE);
        mListBackground.setAlpha(0);

        ty = -50;
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setTranslationY(ty);
        mTvTitle.setAlpha(0);

        mTvClass.setVisibility(View.VISIBLE);
        mTvClass.setTranslationY(ty);
        mTvClass.setAlpha(0);

        mIvChangeCourse.setVisibility(View.VISIBLE);
        mIvChangeCourse.setTranslationY(ty);
        mIvChangeCourse.setAlpha(0f);

        ty = -100;
        mDayPager.setVisibility(View.VISIBLE);
        mCurrentDayPagerDay.setTranslationY(ty);
        mCurrentDayPagerDay.setAlpha(0);
        mCurrentDayPagerNumber.setTranslationY(ty);
        mCurrentDayPagerNumber.setAlpha(0);
        mCurrentDayPagerMonth.setTranslationY(ty);
        mCurrentDayPagerMonth.setAlpha(0);

        mDayIndicator.setVisibility(View.VISIBLE);
        mDayIndicator.setTranslationY(ty);
        mDayIndicator.setAlpha(0);

        ty = 50;
        mMainPager.setVisibility(View.VISIBLE);
        mCurrentMainPagerView.setTranslationY(mMainPager.getTranslationY() + ty);
        mCurrentMainPagerView.setAlpha(0);

        mListBackground.animate().setInterpolator(new DecelerateInterpolator());
        mCurrentDayPagerDay.animate().setInterpolator(new DecelerateInterpolator());
        mCurrentDayPagerNumber.animate().setInterpolator(new DecelerateInterpolator());
        mCurrentDayPagerMonth.animate().setInterpolator(new DecelerateInterpolator());
        mDayIndicator.animate().setInterpolator(new DecelerateInterpolator());
        mCurrentMainPagerView.animate().setInterpolator(new DecelerateInterpolator());

    }

    private void enterMainComponents(long delay) {
        initStartAnimState();

        mHandler.postDelayed(() -> {
            refreshMainPager();
        }, delay);

        delay += 200;

        mHandler.postDelayed(() -> {
            markIndicators();
        }, delay);

        zoom(delay, 400, false);
        delay += 100;

        //moveIn
        long moveInDur = 350;
        float ty = 300;
        mListBackground.animate()
                .setStartDelay(delay)
                .setDuration(moveInDur)
                .translationYBy(-ty)
                .alpha(1)
                .start();

        delay += 200;

        //toolbar
        mDayIndicator.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationY(0)
                .alpha(1)
                .start();
        delay += 100;

        mCurrentDayPagerDay.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationY(0)
                .alpha(1)
                .start();
        delay += 100;

        mCurrentDayPagerMonth.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationY(0)
                .alpha(1)
                .start();
        delay += 100;

        mCurrentDayPagerNumber.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationY(0)
                .alpha(1)
                .start();
        delay += 100;

        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationY(0)
                .alpha(1)
                .start();
        delay += 100;

        mTvClass.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationY(0)
                .alpha(1)
                .start();
        delay += 100;

        mIvChangeCourse.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationY(0)
                .alpha(1)
                .start();

        //content
        delay += 150;
        ty = 50;
        mCurrentMainPagerView.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .alpha(1)
                .translationYBy(-ty)
                .start();

        delay += 0;

        //fab
        mHandler.postDelayed(() -> {
            mStarted = true;
        }, delay);

    }

    private long zoom(long curDelay, long duration, boolean in) {
        //bg scale in
        float scale = in ? 1.1f : 1;
        mIvBackground.animate()
                .setStartDelay(curDelay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(duration)
                .scaleX(scale)
                .scaleY(scale)
                .start();
        //bg darker
        int startColor;
        int endColor;
        if(in) {
            startColor = Color.TRANSPARENT;
            endColor = ContextCompat.getColor(this, R.color.backgroundZoomTint);
        } else {
            startColor = ContextCompat.getColor(this, R.color.backgroundZoomTint);
            endColor = Color.TRANSPARENT;
        }
        ValueAnimator tintAnim = ValueAnimator.ofArgb(startColor, endColor);
        tintAnim.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            mIvBackground.setColorFilter(val);
        });
        tintAnim.setStartDelay(curDelay);
        tintAnim.setInterpolator(new FastOutSlowInInterpolator());
        tintAnim.setDuration(duration);
        tintAnim.start();

        return duration;
    }

    private void startSplash() {
        long delay = 0;

        zoom(delay, 1300, true);

        delay += 150;

        //draw balls
        mHandler.postDelayed(() -> {
            mIvLogo.setVisibility(View.VISIBLE);
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_logo);
            mIvLogo.setImageDrawable(avd);
            avd.start();
        }, delay);

        delay += 2400;
        startLoading(delay);

    }

    private void startLoading(long delay) {
        //show loading view
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setAlpha(0);
        mLoadingView.animate()
                .setInterpolator(new FastOutSlowInInterpolator())
                .setStartDelay(delay)
                .setDuration(400)
                .alpha(1)
                .start();

        delay += 500;

        //load data... internet check
        mHandler.postDelayed(() -> {
            boolean isOnline = isOnline();
            if(isOnline) {
                //Database
                Database.fetchData(this, false);
            } else {
                errorOnLoading(ERR_NO_INTERNET);
            }

//            initDataComponents();
        }, delay);
    }

    private void errorOnLoading(int errorCode) {
        long delay = 0;
        mLoadingView.animate()
                .setStartDelay(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(400)
                .alpha(0)
                .start();

        delay += 400;

        int accentColor = ContextCompat.getColor(this, R.color.four);
        switch(errorCode) {
            case ERR_IO_EXCEPTION:
//                mBtnErrOffline.setVisibility(View.GONE);
                mBtnErrAgain.setTextColor(accentColor);
                mTvErr.setText(R.string.err_io_exception);
                break;

            case ERR_NO_INTERNET:
                mBtnErrOffline.setVisibility(View.VISIBLE);
                mBtnErrOffline.setTextColor(accentColor);
                mBtnErrAgain.setTextColor(Color.WHITE);
                mTvErr.setText(R.string.err_no_internet);
                break;

            case ERR_OTHER_EXCEPTION:
//                mBtnErrOffline.setVisibility(View.GONE);
                mBtnErrAgain.setTextColor(accentColor);
                mTvErr.setText(R.string.err_other_exception);
                break;
        }

        Random r = new Random();
        mIvErr.setImageResource(r.nextInt(2) == 0 ? R.drawable.ic_emj_angry : R.drawable.ic_emj_sad);
        mErrLayout.setVisibility(View.VISIBLE);
        mErrLayout.setAlpha(0f);
        mErrLayout.setTranslationY(-100);
        mErrLayout.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(200)
                .alpha(1)
                .translationY(0)
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Database.load();
        updateClassText();
        ((NumberAdapter) mCourseNumberList.getAdapter()).setNumber(Database.courseNumber);
        ((NumberAdapter) mCourseDegreeList.getAdapter()).setNumber(Database.courseDegree);
        //hide status bar
        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Database.save();
    }

    public void onDatabaseLoaded(boolean ioException, boolean otherException) {
        if(!ioException && !otherException) {
            Database.loaded = true;
            mLoadingView.animate()
                    .setStartDelay(0)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setDuration(400)
                    .alpha(0)
                    .withEndAction(() -> {
                        mLoadingView.setVisibility(View.GONE);
                        mIvLogo.setVisibility(View.GONE);
                        enterMainComponents(100);
                    })
                    .start();
        } else if(ioException) {
            errorOnLoading(ERR_IO_EXCEPTION);
        } else {
            errorOnLoading(ERR_OTHER_EXCEPTION);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void refreshMainPager() {
        if(!Database.loaded) return;
        ((MainPagerAdapter) mMainPager.getAdapter()).update();
    }

    private void markIndicators() {
        for(int i = 0; i < mMainPager.getChildCount(); i++) {
            if(((MainPagerAdapter) mMainPager.getAdapter()).getEntryList(i).getChildCount() > 0) {
                ((DayListAdapter) mDayIndicator.getAdapter()).setDot(i, true);
            } else {
                ((DayListAdapter) mDayIndicator.getAdapter()).setDot(i, false);
            }
        }
    }

}
