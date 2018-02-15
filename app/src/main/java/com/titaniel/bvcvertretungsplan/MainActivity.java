package com.titaniel.bvcvertretungsplan;

import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.date_shower.DateShower;
import com.titaniel.bvcvertretungsplan.day_indicator_list.DayListAdapter;
import com.titaniel.bvcvertretungsplan.day_pager.DayPagerAdapter;
import com.titaniel.bvcvertretungsplan.day_pager.DayPagerTransformer;
import com.titaniel.bvcvertretungsplan.entry_list.EntryItemDecoration;
import com.titaniel.bvcvertretungsplan.entry_list.EntryListAdapter;

public class MainActivity extends AppCompatActivity {

    private static final float HEADER_FADE_SPEED = 3f;

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

    private DateShower mDateShower;

    private int mWidth, mHeight;

    private double mTriRatio = 0;
    private float mTriDegrees = 0;
    private float mTriHypot = 0;

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

        //AppBarLayout
        mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if(verticalOffset < 0) {
                mHeaderClickConsumer.setVisibility(View.VISIBLE);
            } else {
                mHeaderClickConsumer.setVisibility(View.INVISIBLE);
            }
            mHeaderContainer.setAlpha(1 - ((float) Math.abs(verticalOffset) * HEADER_FADE_SPEED)/(float) appBarLayout.getHeight());
        });

        //entryList
        LinearLayoutManager managerEntries =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
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
                ((DayListAdapter)mDayIndicator.getAdapter()).changeSelected(position);
                mDateShower.show(position);
            }
        });

        //adjust views to triangle position
        mTriangle.post(() -> {
            mTriRatio = (double)mTriangle.getHeight()/(double)mTriangle.getWidth();
            mTriDegrees = (float)Math.toDegrees(Math.atan(mTriRatio));
            mTriHypot = (float)Math.hypot((double)mTriangle.getHeight(), (double)mTriangle.getWidth());

            adjustViewHeightToTriangle(mFab, mDayIndicator);

            mDayIndicator.setRotation(mTriDegrees);
        });

        //new LoadingTask().execute(this);
    }

    private void adjustViewHeightToTriangle(View... views) {
        if(views == null) return;
        float extraHeight = mHeaderExtra.getHeight();
        float rightCenter, additionalHeight;
        for(View v : views) {
            if(v == null) continue;
            rightCenter = mWidth-(v.getX()+v.getWidth()/2);
            additionalHeight = rightCenter * (float)mTriRatio;
            v.setTranslationY(-(additionalHeight+extraHeight));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);
    }

    public void onDatabaseLoaded() {
        mEntryList.setAdapter(new EntryListAdapter(this, Database.days.get(0), mTriDegrees));
    }
}
