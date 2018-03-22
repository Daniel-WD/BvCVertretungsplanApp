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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.titaniel.bvcvertretungsplan.date_shower.DateShower;
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
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private ImageView mTriangle;
    private FrameLayout mDateContainer;
    private ConstraintLayout mHeaderContainer;
    private AppBarLayout mAppBarLayout;
    private View mHeaderExtra;
    private TextView mTvClass;
    private FrameLayout mListBackground;
    private LinearLayout mRealCut;
    private LinearLayout mFakeCut;
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

    private View mCurrentDayPagerView, mCurrentMainPagerView;

    private DateShower mDateShower;

    private boolean mFabEnabled = true;
    private boolean mCollapsingEnabled = true;
    private boolean mStarted = false;

    private int mWidth, mHeight;

    private AppBarLayout.Behavior mAppBarBehavior;
    private AppBarLayout.OnOffsetChangedListener mAppBarOffsetChangedListener;

    private double mTriRatio = 0;
    private float mTriDegrees = 0;
    private float mTriHypot = 0;

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
        mToolbar = findViewById(R.id.toolbar);
        mFab = findViewById(R.id.fab);
        mDayPager = findViewById(R.id.dayPager);
        mTriangle = findViewById(R.id.triangle);
        mDateContainer = findViewById(R.id.dateContainer);
        mAppBarLayout = findViewById(R.id.appbarlayout);
        mHeaderContainer = findViewById(R.id.headerContainer);
        mHeaderExtra = findViewById(R.id.extra);
        mTvClass = findViewById(R.id.tvClass);
        mListBackground = findViewById(R.id.listBackground);
        mRealCut = findViewById(R.id.realCut);
        mFakeCut = findViewById(R.id.fakeCut);
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

        //background blurr
        mIvBackground.postDelayed(() -> {
            Blurry.with(MainActivity.this).radius(25).sampling(2).color(ColorUtils.setAlphaComponent(Color.BLACK, 10)).animate().onto(mBgContainer);
        }, 8000);

        //mainPager
        OverScrollDecoratorHelper.setUpOverScroll(mMainPager);
        mMainPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        mMainPager.setOffscreenPageLimit(5);
        mMainPager.post(this::updateCurrentMainpagerView);
        mMainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int oldPos = 0;

            @Override
            public void onPageScrollStateChanged(int state) {
                int newPos = mMainPager.getCurrentItem();
                switch(state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        mFabEnabled = true;
                        mCollapsingEnabled = true;
                        if(newPos != oldPos) {
                            ((MainPagerAdapter) mMainPager.getAdapter()).getEntryList(oldPos).scrollToPosition(0);
                        }
                        oldPos = newPos;
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mFabEnabled = false;
                        mCollapsingEnabled = false;
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        mFabEnabled = false;
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

                updateCurrentMainpagerView();
            }
        });

        //daypager
        mDayPager.setAdapter(new DayPagerAdapter(getSupportFragmentManager()));
        mDayPager.setOffscreenPageLimit(5);
        mDayPager.post(() -> {
            //dateShower
            mDateShower = new DateShower(mDateContainer);

            DayPagerTransformer transformer = new DayPagerTransformer();
            mDayPager.setPageTransformer(false, transformer);

            //adjust date container position
            updateCurrentDayPagerView();
            float topMargin = getResources().getDimensionPixelSize(R.dimen.dateMarginTop);
            mDateContainer.setY(mCurrentDayPagerView.getY() + mCurrentDayPagerView.getHeight() + mDayPager.getY() - mDateContainer.getHeight()/2 + topMargin);

            float secTopMargin = getResources().getDimensionPixelSize(R.dimen.indicatorMarginTop);
            mDayIndicator.setY(mDateContainer.getY() + mDateShower.mTvPrim.getBottom() + secTopMargin);
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
                mDateShower.show(position);
                updateCurrentDayPagerView();
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
        mFab.setOnClickListener(v -> {
            if(!mFabEnabled || mAppBarBehavior.getTopAndBottomOffset() != 0) return;
            mFabEnabled = false;
            if(mCoursePickerLayout.getVisibility() == View.INVISIBLE) {
                showCoursePicker();

                //avd
                AnimatedVectorDrawable sortToCross = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_sort_to_cross, getTheme());
                mFab.setImageDrawable(sortToCross);
                sortToCross.start();
            } else {
                refreshMainPager();
                mHandler.postDelayed(() -> {
                    markIndicators();
                    hideCoursePicker();
                    updateScrollEnabledForPos(mMainPager.getCurrentItem());

                    //avd
                    AnimatedVectorDrawable crossToSort = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_cross_to_sort, getTheme());
                    mFab.setImageDrawable(crossToSort);
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
                            if(flag) {
                                fabTY = mFab.getTranslationY();
                                flag = false;
                            }
                            mFab.setTranslationY(fabTY + verticalOffset);
                            if(verticalOffset < 0) {
                                mFab.hide();
                                mDayPager.setEnabled(false);
                            } else {
                                mFab.show();
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

        //adjust views to triangle position
        mTriangle.post(() -> {
            mTriRatio = (double) mTriangle.getHeight()/(double) mTriangle.getWidth();
            mTriDegrees = (float) Math.toDegrees(Math.atan(mTriRatio));
            mTriHypot = (float) Math.hypot((double) mTriangle.getHeight(), (double) mTriangle.getWidth());

            adjustViewHeightToTriangle(mFab);// TODO: 18.03.2018 change
            //adjustViewHeightToTriangle(mFab, mDayIndicator);

            //mDayIndicator.setTranslationY(mDayIndicator.getTranslationY() - mDayIndicator.getHeight()/4);
            //mDayIndicator.setRotation(mTriDegrees);
        });

        //adjust cut over list background
        mFakeCut.post(() -> {
            mFakeCut.setY(mRealCut.getY());
        });

        hideUi();

        mHandler.postDelayed(() -> {
            startSplash();
        }, 400);
    }

    private void updateCurrentDayPagerView() {
        mCurrentDayPagerView = mDayPager.getChildAt(mDayPager.getCurrentItem()).findViewById(R.id.dayText);
    }

    private void updateCurrentMainpagerView() {
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
        mFabEnabled = false;
        mHandler.postDelayed(() -> mFabEnabled = true, 350/*zoomtime*/);
        mCollapsingEnabled = false;

        ((MainPagerAdapter) mMainPager.getAdapter()).setScrollEnabled(false);
        long delay = 0;
        float ty;

        //zoom
        //zoom(delay, 300, true);

        long headerOutDur = 100;

        //header out
        ty = -50;
        mToolbar.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(headerOutDur)
                .translationYBy(ty)
                .alpha(0)
                .start();
        delay += 30;
        mCurrentDayPagerView.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(headerOutDur)
                .translationYBy(ty)
                .alpha(0)
                .start();
        delay += 30;
        mDateContainer.animate()
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
        ty = mCoursePickerLayout.getY() - mCoursePickerLayout.getTranslationY() - mTriangle.getY() + mTriangle.getHeight()/2;

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
                .withEndAction(() -> mFabEnabled = true)
                .start();

        mRealCut.setVisibility(View.INVISIBLE);
        mFakeCut.setVisibility(View.VISIBLE);
        mFakeCut.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(ty)
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

        //fab
        mFab.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(ty)
                .start();

    }

    private void hideCoursePicker() {
        mFabEnabled = false;
        mHandler.postDelayed(() -> mFabEnabled = true, 300/*zoomtime*/);
        ((MainPagerAdapter) mMainPager.getAdapter()).setScrollEnabled(true);

        updateClassText();

        long delay = 0;
        //move bottom sheet in
        long moveDur = 300;

        //zoom
        //zoom(delay, 300, false);

        float ty = mFakeCut.getY() - mRealCut.getY();

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

        mFakeCut.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(-ty)
                .withEndAction(() -> {
                    mRealCut.setVisibility(View.VISIBLE);
                    mFakeCut.setVisibility(View.INVISIBLE);
                })
                .start();
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
                .start();
        mFab.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .withEndAction(() -> {
                    mCollapsingEnabled = true;
                    mFabEnabled = true;
                    mMainPager.setEnabled(true);
                    mDayPager.setEnabled(true);
                })
                .translationYBy(-ty)
                .start();


        delay += 150;

        //header int
        long headerInDur = 150;

        ty = 100;

        mDayIndicator.setTranslationY(mDayIndicator.getTranslationY()-(ty-50));
        mDateContainer.setTranslationY(mDateContainer.getTranslationY()-(ty-50));
        mCurrentDayPagerView.setTranslationY(mCurrentDayPagerView.getTranslationY()-(ty-50));
        mToolbar.setTranslationY(mToolbar.getTranslationY()-(ty-50));


        mDayIndicator.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();
        delay += 30;
        mDateContainer.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();
        delay += 30;
        mCurrentDayPagerView.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();
        delay += 30;
        mToolbar.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(headerInDur)
                .translationYBy(ty)
                .alpha(1)
                .start();

    }

    private void hideUi() {
        mRealCut.setVisibility(View.INVISIBLE);
        mFakeCut.setVisibility(View.INVISIBLE);
        mMainPager.setVisibility(View.INVISIBLE);
        mListBackground.setVisibility(View.INVISIBLE);
        mToolbar.setVisibility(View.INVISIBLE);
        mDayPager.setVisibility(View.INVISIBLE);
        mDateContainer.setVisibility(View.INVISIBLE);
        mDayIndicator.setVisibility(View.INVISIBLE);
        mFab.setVisibility(View.INVISIBLE);
        mFab.hide();
    }

    private void initStartAnimState() {
        float ty = 300;
        mFakeCut.setTranslationY(mFakeCut.getTranslationY() + ty);
        mFakeCut.setVisibility(View.VISIBLE);
        mFakeCut.setAlpha(0);

        mListBackground.setTranslationY(mListBackground.getTranslationY() + ty);
        mListBackground.setVisibility(View.VISIBLE);
        mListBackground.setAlpha(0);

        ty = -50;
        mToolbar.setVisibility(View.VISIBLE);
        mToolbar.setTranslationY(ty);
        mToolbar.setAlpha(0);

        ty = -50;
        mDayPager.setVisibility(View.VISIBLE);
        mCurrentDayPagerView.setTranslationY(ty);
        mCurrentDayPagerView.setAlpha(0);

        ty = -100;
        mDateContainer.setVisibility(View.VISIBLE);
        mDateContainer.setTranslationY(mDateContainer.getTranslationY() + ty);
        mDateContainer.setAlpha(0);

        mDayIndicator.setVisibility(View.VISIBLE);
        mDayIndicator.setTranslationY(mDayIndicator.getTranslationY() + ty);
        mDayIndicator.setAlpha(0);

        ty = 50;
        mMainPager.setVisibility(View.VISIBLE);
        mCurrentMainPagerView.setTranslationY(mMainPager.getTranslationY() + ty);
        mCurrentMainPagerView.setAlpha(0);

        mFakeCut.animate().setInterpolator(new DecelerateInterpolator());
        mListBackground.animate().setInterpolator(new DecelerateInterpolator());
        mToolbar.animate().setInterpolator(new DecelerateInterpolator());
        mDayPager.animate().setInterpolator(new DecelerateInterpolator());
        mDateContainer.animate().setInterpolator(new DecelerateInterpolator());
        mDayIndicator.animate().setInterpolator(new DecelerateInterpolator());
        mMainPager.animate().setInterpolator(new DecelerateInterpolator());

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
        mFakeCut.animate()
                .setStartDelay(delay)
                .setDuration(moveInDur)
                .withEndAction(() -> {
                    mFakeCut.setVisibility(View.INVISIBLE);
                    mRealCut.setVisibility(View.VISIBLE);
                })
                .translationYBy(-ty)
                .alpha(1)
                .start();

        mListBackground.animate()
                .setStartDelay(delay)
                .setDuration(moveInDur)
                .translationYBy(-ty)
                .alpha(1)
                .start();

        delay += 200;

        //toolbar
        ty = -100;

        mDayIndicator.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationYBy(-ty)
                .alpha(1)
                .start();
        delay += 100;
        mDateContainer.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationYBy(-ty)
                .alpha(1)
                .start();
        delay += 100;
        mCurrentDayPagerView.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .translationY(0)
                .alpha(1)
                .start();
        delay += 100;
        mToolbar.animate()
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
            mFab.show();
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
                mBtnErrOffline.setVisibility(View.GONE);
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
                mBtnErrOffline.setVisibility(View.GONE);
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

    private void adjustViewHeightToTriangle(View... views) {
        if(views == null) return;
        float extraHeight = mHeaderExtra.getHeight();
        float rightCenter, additionalHeight;
        for(View v : views) {
            if(v == null) continue;
            rightCenter = mWidth - (v.getX() + v.getWidth()/2);
            additionalHeight = rightCenter*(float) mTriRatio + v.getY() - mAppBarLayout.getHeight() + mToolbar.getY()/*equals status bar height*/;
            v.setTranslationY(-(additionalHeight + extraHeight));
        }
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
