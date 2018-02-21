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
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import com.titaniel.bvcvertretungsplan.entry_list.EntryItemDecoration;
import com.titaniel.bvcvertretungsplan.entry_list.EntryListAdapter;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String[] sDegrees = {"5", "6", "7", "8", "9", "10", "11", "12"};
    private static final String[] sNumbers = {"1", "2", "3", "4", "5", "6"};

    private static final float HEADER_FADE_SPEED = 3f;

    private static final int ERR_NO_INTERNET = 0;
    private static final int ERR_IO_EXCEPTION = 1;
    private static final int ERR_OTHER_EXCEPTION = 2;

    private RecyclerView mDayIndicator;
    private RecyclerView mEntryList;
    private ViewPager mDayPager;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private ImageView mTriangle;
    private FrameLayout mDateContainer;
    private ConstraintLayout mHeaderContainer;
    private AppBarLayout mAppBarLayout;
    private View mHeaderExtra;
    private View mHeaderClickConsumer;
    private TextView mTvClass;
    private FrameLayout mListBackground;
    private LinearLayout mRealCut;
    private LinearLayout mFakeCut;
    private ImageView mIvBackground;
    private ImageView mIvLogo, mIvLogoBvCFilled, mIvLogoBvCOutline;
    private FrameLayout mLogoContainer;
    private AVLoadingIndicatorView mLoadingView;
    private ImageView mIvErr;
    private LinearLayout mErrLayout;
    private TextView mTvErr;
    private Button mBtnErrAgain, mBtnErrOffline;
    private LinearLayout mCoursePickerLayout;
    private RecyclerView mCourseDegreeList, mCourseNumberList;
    private TextView mTvCourseDegree, mTvCourseNumber;
    private View mGlobalConsumer;

    private DateShower mDateShower;

    private boolean mEntryListScrollEnabled = true;
    private boolean mCollapsingEnabled = true;

    private int mWidth, mHeight;

    private double mTriRatio = 0;
    private float mTriDegrees = 0;
    private float mTriHypot = 0;

    private Handler mHandler = new Handler();

    private float mCoursePickerShowMoveDistance;

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
        mEntryList = findViewById(R.id.entryList);
        mAppBarLayout = findViewById(R.id.appbarlayout);
        mHeaderContainer = findViewById(R.id.headerContainer);
        mHeaderExtra = findViewById(R.id.extra);
        mHeaderClickConsumer = findViewById(R.id.headerClickConsumer);
        mTvClass = findViewById(R.id.tvClass);
        mListBackground = findViewById(R.id.listBackground);
        mRealCut = findViewById(R.id.realCut);
        mFakeCut = findViewById(R.id.fakeCut);
        mIvBackground = findViewById(R.id.background);
        mIvLogo = findViewById(R.id.ivLogo);
        mIvLogoBvCFilled = findViewById(R.id.ivLogoBvCFilled);
        mLogoContainer = findViewById(R.id.logoContainer);
        mLoadingView = findViewById(R.id.loadingView);
        mIvLogoBvCOutline = findViewById(R.id.ivLogoBvCOutline);
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
        mGlobalConsumer = findViewById(R.id.globalConsumer);

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

                    updateClassText();

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

        //mCourseDegreeList
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

                    updateClassText();
        }));
        mCourseNumberList.setHasFixedSize(true);

        //fab
        mFab.setOnClickListener(v -> {
            if(mCoursePickerLayout.getVisibility() == View.INVISIBLE) {
                showCoursePicker();
                mFab.setImageResource(R.drawable.ic_close);
            } else {
                refreshEntryList();
                hideCoursePicker();
                mFab.setImageResource(R.drawable.ic_sort_variant);
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
            mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

                boolean flag = true;
                float fabTY = 0;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if(!mCollapsingEnabled) return;
                    if(flag) {
                        fabTY = mFab.getTranslationY();
                        flag = false;
                    }
                    mFab.setTranslationY(fabTY + verticalOffset);
                    if(verticalOffset < 0) {
                        mHeaderClickConsumer.setVisibility(View.VISIBLE);
                        if(mFab.getVisibility() == View.VISIBLE) {
                            mFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                                @Override
                                public void onHidden(FloatingActionButton fab) {
                                    mFab.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } else {
                        mHeaderClickConsumer.setVisibility(View.INVISIBLE);
                        if(mFab.getVisibility() == View.VISIBLE) {
                            mFab.show();
                        }
                    }
                    mHeaderContainer.setAlpha(1 - ((float) Math.abs(verticalOffset)*HEADER_FADE_SPEED)/(float) appBarLayout.getHeight());
                }
            });
        }, 100);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return mCollapsingEnabled;
            }
        });
        params.setBehavior(behavior);

        //entryList
        LinearLayoutManager managerEntries =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
                    @Override
                    public boolean canScrollVertically() {
                        return mEntryListScrollEnabled && super.canScrollVertically();
                    }
                };
        mEntryList.setLayoutManager(managerEntries);
        mEntryList.addItemDecoration(new EntryItemDecoration(this));

        //dateShower
        mDateContainer.post(() -> mDateShower = new DateShower(mDateContainer));

        //days indicator
        LinearLayoutManager managerDays =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mDayIndicator.setLayoutManager(managerDays);
        mDayIndicator.setAdapter(new DayListAdapter(mDayIndicator, mDayPager));
        mDayIndicator.setHasFixedSize(true);

        //days pager
        mDayPager.setAdapter(new DayPagerAdapter(getSupportFragmentManager()));
        mDayPager.post(() -> {
            DayPagerTransformer transformer = new DayPagerTransformer();
//            transformer.setRatio(mTriRatio);
            mDayPager.setPageTransformer(false, transformer);

            //adjust date container position
            View v = mDayPager.getChildAt(0).findViewById(R.id.dayText);
            float topMargin = getResources().getDimensionPixelSize(R.dimen.dateMarginTop);
            mDateContainer.setY(v.getY() + v.getHeight() + mDayPager.getY() - mDateContainer.getHeight()/2 + topMargin);

        });
        mDayPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrollStateChanged(int state) {}
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageSelected(int position) {
                mDayIndicator.post(() -> {
                    ((DayListAdapter) mDayIndicator.getAdapter()).changeSelected(position);
                });
                mDateShower.show(position);
                refreshEntryList();
            }
        });

        //adjust views to triangle position
        mTriangle.post(() -> {
            mTriRatio = (double) mTriangle.getHeight()/(double) mTriangle.getWidth();
            mTriDegrees = (float) Math.toDegrees(Math.atan(mTriRatio));
            mTriHypot = (float) Math.hypot((double) mTriangle.getHeight(), (double) mTriangle.getWidth());

            adjustViewHeightToTriangle(mFab, mDayIndicator);

            mDayIndicator.setTranslationY(mDayIndicator.getTranslationY() - mDayIndicator.getHeight()/4);
            mDayIndicator.setRotation(mTriDegrees);
        });

        //adjust cut over list background
        mFakeCut.post(() -> {
            mFakeCut.setY(mRealCut.getY());
        });

        initStartAnimState();

        mHandler.postDelayed(() -> {
            startSplash();
        }, 400);
    }

    private void updateClassText() {
        if(Integer.parseInt(Database.courseDegree) > 10) {
            mTvClass.setText("  -  " + getString(R.string.temp_class, Database.courseDegree));
        } else {
            mTvClass.setText("  -  " + getString(R.string.temp_class, Database.courseDegree + "/" + Database.courseNumber));
        }
    }

    private void showCoursePicker() {
        mGlobalConsumer.setVisibility(View.VISIBLE); //only 300 ms
        mHandler.postDelayed(() -> mGlobalConsumer.setVisibility(View.INVISIBLE), 300/*zoomtime*/);
        mCollapsingEnabled = false;

        mEntryListScrollEnabled = false;
        long delay = 0;
        //move bottom sheet out
        long moveDur = 300;
//        float ty = (mHeight - mTriangle.getY());
        float ty = mCoursePickerLayout.getY() - mTriangle.getY() + mTriangle.getHeight()/2;

        mEntryList.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .alpha(0f)
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

        mEntryList.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(ty)
                .start();

        mFab.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(ty)
                .start();

        //header out
        mAppBarLayout.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(150)
                .translationYBy(-50)
                .alpha(0)
                .start();

        delay += 100;

        //zoom
        zoom(delay, 300, true);

        //show picker
        mCoursePickerLayout.setVisibility(View.VISIBLE);
        mCoursePickerLayout.setAlpha(0f);
        mCoursePickerLayout.setTranslationY(-50);
        mCoursePickerLayout.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .alpha(1)
                .translationY(0)
                .start();

    }

    private void hideCoursePicker() {
        mGlobalConsumer.setVisibility(View.VISIBLE);
        mHandler.postDelayed(() -> mGlobalConsumer.setVisibility(View.INVISIBLE), 300/*zoomtime*/);
        mEntryListScrollEnabled = true;
        long delay = 0;
        //move bottom sheet in
        long moveDur = 300;

        //zoom
        zoom(delay, 300, false);

        //show picker
        mCoursePickerLayout.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .withEndAction(() -> {
                    mCoursePickerLayout.setVisibility(View.INVISIBLE);
                })
                .alpha(0)
                .translationY(-50)
                .start();

        delay += 0;

        float ty = mFakeCut.getY() - mRealCut.getY();


        mEntryList.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .alpha(1f)
                .start();

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

        mEntryList.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .translationYBy(-ty)
                .start();

        mFab.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(moveDur)
                .withEndAction(() -> {
                    mCollapsingEnabled = true;
                })
                .translationYBy(-ty)
                .start();

        delay += 100;

        //header out
        mAppBarLayout.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(150)
                .translationYBy(50)
                .alpha(1)
                .start();

    }

    private void initStartAnimState() {
        mRealCut.setVisibility(View.INVISIBLE);
        mFakeCut.setVisibility(View.INVISIBLE);
        mEntryList.setVisibility(View.INVISIBLE);
        mListBackground.setVisibility(View.INVISIBLE);
        mToolbar.setVisibility(View.INVISIBLE);
        mDayPager.setVisibility(View.INVISIBLE);
        mDateContainer.setVisibility(View.INVISIBLE);
        mDayIndicator.setVisibility(View.INVISIBLE);
        mFab.setVisibility(View.INVISIBLE);
        mFab.hide();

    }

    private void enterMainComponents(long delay) {

        //moveIn
        long moveInDur = 400;
        float ty = mHeight - mTriangle.getY();
        mFakeCut.setTranslationY(mFakeCut.getTranslationY() + ty);
        mFakeCut.setVisibility(View.VISIBLE);
        mFakeCut.animate()
                .setStartDelay(delay)
                .setDuration(moveInDur)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    mFakeCut.setVisibility(View.INVISIBLE);
                    mRealCut.setVisibility(View.VISIBLE);
                })
                .translationYBy(-ty)
                .start();

        mEntryList.setTranslationY(mEntryList.getTranslationY() + ty);
        mEntryList.setVisibility(View.VISIBLE);
        mEntryList.animate()
                .setStartDelay(delay)
                .setDuration(moveInDur)
                .setInterpolator(new DecelerateInterpolator())
                .translationYBy(-ty)
                .start();

        mListBackground.setTranslationY(mListBackground.getTranslationY() + ty);
        mListBackground.setVisibility(View.VISIBLE);
        mListBackground.animate()
                .setStartDelay(delay)
                .setDuration(moveInDur)
                .setInterpolator(new DecelerateInterpolator())
                .translationYBy(-ty)
                .start();

        delay += moveInDur - 50;

        //toolbar
        mToolbar.setVisibility(View.VISIBLE);
        mToolbar.setTranslationY(mToolbar.getHeight()/3);
        mToolbar.setAlpha(0);
        mToolbar.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .translationY(0)
                .alpha(1)
                .start();

        delay += 50;

        //dayPager
        mDayPager.setVisibility(View.VISIBLE);
        mDayPager.setTranslationX(-100);
        mDayPager.setAlpha(0);
        mDayPager.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .translationX(0)
                .alpha(1)
                .start();

        delay += 150;

        //dateContainer
        mDateContainer.setVisibility(View.VISIBLE);
        ty = mDateContainer.getHeight()/4;
        mDateContainer.setTranslationY(mDateContainer.getTranslationY() + ty);
        mDateContainer.setAlpha(0);
        mDateContainer.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .translationYBy(-ty)
                .alpha(1)
                .start();

        delay += 150;

        //indicator
        mDayIndicator.setVisibility(View.VISIBLE);
        ty = -mDayIndicator.getHeight()/2;
        mDayIndicator.setTranslationY(mDayIndicator.getTranslationY() + ty);
        mDayIndicator.setAlpha(0);
        mDayIndicator.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .translationYBy(-ty)
                .alpha(1)
                .start();

        delay += 200;

        //fab
        mHandler.postDelayed(() -> mFab.show(), delay);

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

        delay += zoom(delay, 600, true) -50;

        //draw balls
        mHandler.postDelayed(() -> {
            mIvLogo.setVisibility(View.VISIBLE);
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_draw_logo);
            mIvLogo.setImageDrawable(avd);
            avd.start();
        }, delay);

        delay += 1100;

        //fill with bvc
        mIvLogoBvCFilled.setVisibility(View.VISIBLE);
        mIvLogoBvCFilled.setAlpha(0f);
        mIvLogoBvCFilled.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(700)
                .alpha(1)
                .withEndAction(() -> {
                    long d = 50;
                    //switch fill to outline with bvc ... cross fade
                    mIvLogoBvCFilled.animate()
                            .setStartDelay(d)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(700)
                            .alpha(0)
                            .start();
                    mIvLogoBvCOutline.setVisibility(View.VISIBLE);
                    mIvLogoBvCOutline.setAlpha(0f);
                    mIvLogoBvCOutline.animate()
                            .setStartDelay(d)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(700)
                            .alpha(1)
                            .start();
                })
                .start();
        delay += 900;
        //scale out with alpha
        mLogoContainer.animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(delay)
                .setDuration(700)
                .alpha(0)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .start();

        delay += 1200;
        startLoading(delay);

    }

    private void startLoading(long delay) {
        //show loading view
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setAlpha(0);
        mLoadingView.animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(delay)
                .setDuration(500)
                .alpha(1)
                .start();

        delay += 1000;

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
            additionalHeight = rightCenter* (float)mTriRatio +  v.getY() - mAppBarLayout.getHeight() + mToolbar.getY()/*equals status bar height*/;
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
            refreshEntryList();

            zoom(0, 600, false);
            mLoadingView.animate()
                    .setStartDelay(0)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(400)
                    .alpha(0)
                    .withEndAction(() -> {
                        mLoadingView.setVisibility(View.GONE);
                        mLogoContainer.setVisibility(View.GONE);
                        enterMainComponents(0);
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

    private void refreshEntryList() {
        if(!Database.loaded) return;
        Database.Entry[] entries = Database.findEntriesByCourse(
                DayManager.dates[mDayPager.getCurrentItem()],
                Integer.parseInt(Database.courseDegree),
                Integer.parseInt(Database.courseNumber));
        if(entries == null) {
            entries = new Database.Entry[0];
        }
        mEntryList.setAdapter(new EntryListAdapter(this, entries));
    }
}
