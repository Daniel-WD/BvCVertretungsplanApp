package com.titaniel.bvcvertretungsplan;

import android.graphics.Point;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.day_list.DayListAdapter;
import com.titaniel.bvcvertretungsplan.day_pager.DayPagerAdapter;
import com.titaniel.bvcvertretungsplan.day_pager.DayPagerTransformer;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mDayIndicator;
    private ViewPager mDayPager;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private ImageView mTriangle;

    private int mWidth, mHeight;

    private float mTriRatio = 0;
    private float mTriDegrees = 0;

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

        LinearLayoutManager mManagerDays =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        //day indicator
        mDayIndicator.setLayoutManager(mManagerDays);
        mDayIndicator.setAdapter(new DayListAdapter(mDayIndicator));
        mDayIndicator.setHasFixedSize(true);

        //day pager
        mDayPager.setAdapter(new DayPagerAdapter(getSupportFragmentManager()));
        mDayPager.post(() -> {
            DayPagerTransformer transformer = new DayPagerTransformer();
//            transformer.setRatio(mTriRatio);
            mDayPager.setPageTransformer(false, transformer);
        });

        //adjust fab position
        mTriangle.post(() -> {
            mTriRatio = (float)mTriangle.getHeight()/(float)mTriangle.getWidth();
            mTriDegrees = (float)Math.toDegrees(Math.atan(mTriRatio));
            adjustViewHeightToTriangle(mFab, mDayIndicator);
            mDayIndicator.setRotation(mTriDegrees);
        });

        //new LoadingTask().execute(this);
    }

    private void adjustViewHeightToTriangle(View... views) {
        if(views == null) return;
        float rightCenter, additionalHeight;
        for(View v : views) {
            if(v == null) continue;
            rightCenter = mWidth-(v.getX()+v.getWidth()/2);
            additionalHeight = rightCenter * mTriRatio;
            v.setTranslationY(-additionalHeight);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
