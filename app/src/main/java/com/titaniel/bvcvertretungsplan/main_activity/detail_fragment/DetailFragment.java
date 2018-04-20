package com.titaniel.bvcvertretungsplan.main_activity.detail_fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.rd.PageIndicatorView;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.ViewUtils;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.main_activity.course_settings_fragment.NumberAdapter;
import com.titaniel.bvcvertretungsplan.main_activity.detail_fragment.header_pager.HeaderPagerAdapter;
import com.titaniel.bvcvertretungsplan.main_activity.detail_fragment.header_pager.HeaderPagerTransformer;
import com.titaniel.bvcvertretungsplan.main_activity.detail_fragment.list_pager.EntryPagerAdapter;
import com.titaniel.bvcvertretungsplan.main_activity.detail_fragment.list_pager.entry_list.EntryListAdapter;
import com.titaniel.bvcvertretungsplan.my_viewpager.MyViewPager;

public class DetailFragment extends Fragment {

    private static final float HEADER_FADE_SPEED = 3f;

    private View mRoot;
    private MyViewPager mDayPager;
    private RelativeLayout mHeaderContainer;
    private AppBarLayout mAppBarLayout;
    private MyViewPager mMainPager;
    private PageIndicatorView mIndicator;
    private View mVDivLeft, mVDivRight;

    private View mDayPagerDay, mDayPagerNumber, mDayPagerMonth;
    private RecyclerView mMainPagerList;

    private boolean mCollapsingEnabled = true;
    private boolean mStarted = false;

    private AppBarLayout.Behavior mAppBarBehavior;
    private AppBarLayout.OnOffsetChangedListener mAppBarOffsetChangedListener;

    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mRoot = getView();

        //Init
        mDayPager = mRoot.findViewById(R.id.dayPager);
        mAppBarLayout = mRoot.findViewById(R.id.appbarlayout);
        mHeaderContainer = mRoot.findViewById(R.id.headerContainer);
        mMainPager = mRoot.findViewById(R.id.mainPager);
        mIndicator = mRoot.findViewById(R.id.indicator);
        mVDivRight = mRoot.findViewById(R.id.vDivRight);
        mVDivLeft = mRoot.findViewById(R.id.vDivLeft);

        //mainPager
//        OverScrollDecoratorHelper.setUpOverScroll(mMainPager);
        mMainPager.setOffscreenPageLimit(5);
        mMainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            int oldPos = 0;

            @Override
            public void onPageScrollStateChanged(int state) {
                int newPos = mMainPager.getCurrentItem();
                switch(state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        mCollapsingEnabled = true;
                        if(newPos != oldPos) {
                            ((EntryPagerAdapter) mMainPager.getAdapter()).getEntryList(oldPos).scrollToPosition(0);
                        }
                        oldPos = newPos;
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mCollapsingEnabled = false;
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        mCollapsingEnabled = true;
                        mDayPager.setCurrentItem(newPos, true);
                        updateScrollEnabledForPos(newPos);
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {
                mCollapsingEnabled = true;
                if(mRoot.getHeight() - mAppBarLayout.getHeight() >
                        ((EntryPagerAdapter) mMainPager.getAdapter()).getEntryList(position).getHeight()
                        || mAppBarLayout.getTop() >= -0.1*mAppBarLayout.getHeight()) {
                    mAppBarLayout.setExpanded(true, true);
                }
                updateCurrentMainPagerView();
            }
        });

        //dayPager
        mDayPager.setScrollDurationFactor(2.5);
        mDayPager.setOffscreenPageLimit(5);
        HeaderPagerTransformer transformer = new HeaderPagerTransformer();
        mDayPager.setPageTransformer(false, transformer);
        mDayPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrollStateChanged(int state) { }
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                mMainPager.setCurrentItem(position, true);
                updateCurrentDayPagerViews();
            }
        });

        //AppBarLayout
        //wait to prevent fab position
        mAppBarLayout.addOnOffsetChangedListener(
                mAppBarOffsetChangedListener = (appBarLayout, verticalOffset) -> {
                    if(!mCollapsingEnabled || !mStarted) return;
                    if(verticalOffset < 0) {
                        mDayPager.setEnabled(false);
                    } else {
                        mDayPager.setEnabled(true);
                    }
                    // TODO: 29.03.2018 threshold
                    mHeaderContainer.setAlpha(1 - ((float) Math.abs(verticalOffset)*HEADER_FADE_SPEED)/(float) appBarLayout.getHeight());
                });

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        mAppBarBehavior = new AppBarLayout.Behavior();
        mAppBarBehavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(mAppBarBehavior);

        //mRoot.setAlpha(0.01f);
//        ViewUtils.setNestedEnabled(mRoot, false);
//        mHandler.postDelayed(() -> mRoot.setVisibility(View.INVISIBLE), 10000);
        mRoot.setVisibility(View.INVISIBLE);
    }

    public void show(long delay) {
        mRoot.setVisibility(View.VISIBLE);

        //day
        mDayPagerDay.setAlpha(0);
        mDayPagerDay.setTranslationY(100);
        mDayPagerDay.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //date
        delay += 50;
        mDayPagerMonth.setAlpha(0);
        mDayPagerMonth.setTranslationY(100);
        mDayPagerMonth.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        mDayPagerNumber.setAlpha(0);
        mDayPagerNumber.setTranslationY(100);
        mDayPagerNumber.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //indicator
        delay += 50;
        mIndicator.setAlpha(0);
        mIndicator.setTranslationY(100);
        mIndicator.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //dividers
        delay += 50;
        mVDivLeft.setAlpha(0);
        mVDivLeft.setTranslationY(100);
        mVDivLeft.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .alpha(1)
                .translationY(0)
                .start();
        mVDivRight.setAlpha(0);
        mVDivRight.setTranslationY(100);
        mVDivRight.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        delay += 50;

//        for(int i = 0; i < mMainPager.getChildCount(); i++) {
//            if(i == mMainPager.getCurrentItem()) continue;
//            View v = mMainPager.getChildAt(i);
//            ((EntryListAdapter) ((RecyclerView) v.findViewById(R.id.entryList)).getAdapter()).makeVisible();
//        }
        ((EntryListAdapter) mMainPagerList.getAdapter()).show(delay);
    }

    public long hide(long delay) {
        delay += ((EntryListAdapter) mMainPagerList.getAdapter()).hide(delay);

        delay += 20;

        //indicator
        mIndicator.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutLinearInInterpolator())
                .alpha(0)
                .translationY(100)
                .start();

        //dividers
        delay += 10;
        mVDivLeft.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutLinearInInterpolator())
                .alpha(0)
                .translationY(100)
                .start();
        mVDivRight.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutLinearInInterpolator())
                .alpha(0)
                .translationY(100)
                .start();

        //date
        delay += 10;
        mDayPagerMonth.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutLinearInInterpolator())
                .alpha(0)
                .translationY(100)
                .withEndAction(() -> mDayPagerMonth.setTranslationY(0))
                .start();
        mDayPagerNumber.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutLinearInInterpolator())
                .alpha(0)
                .translationY(100)
                .withEndAction(() -> mDayPagerNumber.setTranslationY(0))
                .start();

        //day
        delay += 10;
        mDayPagerDay.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new FastOutLinearInInterpolator())
                .alpha(0)
                .translationY(100)
                .withEndAction(() -> {
                    mDayPagerDay.setTranslationY(0);
                    mRoot.setVisibility(View.INVISIBLE);
                })
                .start();

        delay += 50;

        return delay;
    }

    public int getCurrentIndex() {
        return mDayPager.getCurrentItem();
    }

    public void setCurrentIndex(int index) {
        mMainPager.setCurrentItem(index, false);
        mDayPager.setCurrentItem(index, false);
    }

    private void updateCurrentDayPagerViews() {
        mDayPagerDay = mDayPager.getChildAt(mDayPager.getCurrentItem()).findViewById(R.id.dayText);
        mDayPagerMonth = mDayPager.getChildAt(mDayPager.getCurrentItem()).findViewById(R.id.tvMonthYear);
        mDayPagerNumber = mDayPager.getChildAt(mDayPager.getCurrentItem()).findViewById(R.id.tvDayInMonth);
    }

    private void updateCurrentMainPagerView() {
        mMainPagerList = mMainPager.getChildAt(mMainPager.getCurrentItem()).findViewById(R.id.entryList);
    }

    private void updateScrollEnabledForPos(int pos) {
        EntryPagerAdapter adapter = ((EntryPagerAdapter) mMainPager.getAdapter());
        if(mRoot.getHeight() - mAppBarLayout.getHeight() > adapter.getEntryList(pos).getHeight() || adapter.getEntryList(pos).getChildCount() == 0) {
            mAppBarLayout.setExpanded(true, true);
            adapter.getFragment(pos).scrollEnabled = false;
        } else {
            adapter.getFragment(pos).scrollEnabled = true;
        }
    }

    public void onDatabaseLoaded() {
        mMainPager.setAdapter(new EntryPagerAdapter(getChildFragmentManager()));
        mDayPager.setAdapter(new HeaderPagerAdapter(getChildFragmentManager()));

        mMainPager.postDelayed(this::updateCurrentMainPagerView, 100);
        mDayPager.postDelayed(this::updateCurrentDayPagerViews, 100);

        if(mMainPager.getAdapter().getCount() == 1) {
            mVDivLeft.setPivotX(0);
            mVDivLeft.setScaleX(1.5f);
            mIndicator.setVisibility(View.GONE);
        } else {
            mVDivLeft.setPivotX(0);
            mVDivLeft.setScaleX(1f);
            mIndicator.setVisibility(View.VISIBLE);
        }

        //indicator
        mIndicator.setViewPager(mMainPager);


        mHandler.postDelayed(() -> {
//            mRoot.setAlpha(1);
        }, 1000);
    }

    public void update() {
        if(!Database.loaded) return;
        ((EntryPagerAdapter) mMainPager.getAdapter()).update();
    }

}
