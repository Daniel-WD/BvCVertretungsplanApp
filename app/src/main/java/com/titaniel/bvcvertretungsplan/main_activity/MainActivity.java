package com.titaniel.bvcvertretungsplan.main_activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.titaniel.bvcvertretungsplan.main_activity.class_settings.ClassSettingsFragment;
import com.titaniel.bvcvertretungsplan.main_activity.day_list.DayListAdapter;
import com.titaniel.bvcvertretungsplan.main_activity.error_fragment.ErrorFragment;
import com.titaniel.bvcvertretungsplan.main_activity.login_fragment.LoginFragment;
import com.titaniel.bvcvertretungsplan.utils.DateManager;
import com.titaniel.bvcvertretungsplan.utils.Utils;
import com.victor.loading.rotate.RotateLoading;

/**
 * Repräsentiert das Hauptfenster, also den Frame, der den ganzen Bildschirm ausfüllt.
 * Hier befindet sich die Anzeige des Vertretungsplans, es wird jedoch auch die Anzeige anderer
 * Fragmente(zum Beispiel ErrorFragment oder LoginFragment) geregelt
 */
public class MainActivity extends AppCompatActivity {

    //States für jedes Fragment
    private static final int FM_NONE = 0, FM_COURSE = 1, FM_LOGIN = 2, FM_ERROR = 3, LOADING = 4;
    private int mState = FM_NONE; //aktueller Fragmentstatus, sagt ob und welches Fragment gerade angezeigt wird

    /*
      Um den Sinn dieses Runnables zu verstehen, muss man sich wirlklich mit Listen in Android beschäftigt haben.
      Dieser Code gehört zu einem Abschnitt, der Laggs(Hänger) beim Scrollen verhindern soll. Diese Lösung ist nicht
      offiziell, funktioniert aber.
      Bevor dieser Code ausgeführt wird, wird die Liste zum langsamen nach unten Scrollen gezwungen. Dabei werden
      im Adapter der Liste, bestimmte zeitaufwändige Methoden ausgeführt, die zu Laggs führen. Diese Methoden
      werden jedoch nur einmal ausgeführt. Sodass später, wenn der Nutzer nach unten scrollt, keine Laggs auftreten.
      Sofort nachdem der Befehl zum langsamen nach unten scrollen gegeben wurde, wird dieses Runnable ausgeführt.
      Dieses ruft sich solange selbst auf, bis die Liste ganz unten angekommen ist, und gibt dann bescheid, dass die
      Liste wieder angezeigt werden kann.

      Das ganze wird beim starten und nachdem eine neue Klasse gesetzt wurde ausgeführt.
      Das Runnable wird in der Methode <code>fillList</code> ausgeführt
     */
    private Runnable mRBringDayListToTop = new Runnable() {

        boolean wasNotIdle = false;

        @Override
        public void run() {
            if(mDayList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && wasNotIdle) {
                mDayList.scrollToPosition(0);

                mHandler.postDelayed(() -> {
                    if(mState == FM_NONE) {
                        enterMainComponents(0);
                    } else {
                        onBackPressed();
                    }
                }, 10);
            } else {
                wasNotIdle = true;
                mHandler.post(this);
            }
        }
    };

    private boolean mHeaderFadeEnabled = false;
    private boolean mHeaderVisible = true;
    private boolean isPaused = false;

    private ImageView mIvBackground;
    private ImageView mIvTitle;
    private RotateLoading mLoadingView;
    private ImageView mIvEdit;
    private View mVBgOverlay;
    private LinearLayout mLyClass;
    private TextView mTvClass;
    private View mLyNothing;

    //list
    private RecyclerView mDayList;
    private DayListAdapter mDayListAdapter;

    private ErrorFragment mErrorFragment;
    private ClassSettingsFragment mClassSettingsFragment;
    private LoginFragment mLoginFragment;

    private float mFixedFirstItemPosition;

    private Handler mHandler = new Handler();

    private boolean mBlockButtons = false;

    /**
     * Hier ist der Start-Punkt der ganzen Anwendung. Hier werden alle Initialisierungen ausgeführt.
     * @param savedInstanceState Android spezifisch... wird nicht genutzt deswegen erkläre ich das jetzt nicht :D
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        mIvTitle = findViewById(R.id.ivTit);
        mLoadingView = findViewById(R.id.loadingView);
        mIvEdit = findViewById(R.id.ivEdit);
        mDayList = findViewById(R.id.dayList);
        mIvBackground = findViewById(R.id.ivBackground);
        mVBgOverlay = findViewById(R.id.vBackgroundOverlay);
        mLyClass = findViewById(R.id.lyClass);
        mTvClass = findViewById(R.id.tvClass);
        mLyNothing = findViewById(R.id.lyNothing);

        mLoadingView.start();

        //init fragments
        mErrorFragment = (ErrorFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentErr);
        mClassSettingsFragment = (ClassSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentCourseSettings);
        mLoginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentLogin);

        //errorFragment
        mErrorFragment.setErrorFragmentCallback(new ErrorFragment.ErrorFragmentCallback() {
            @Override
            public void onBtnAgainClicked(Button button) {
                if(mBlockButtons) return;
                blockButtons(2000);
                mErrorFragment.hide(0);
                login(300);
                mState = FM_NONE;
            }

            @Override
            public void onBtnOfflineClicked(Button button) {
                if(mBlockButtons) return;
                blockButtons(2000);
                mErrorFragment.hide(0);
                Database.fetchData(MainActivity.this, true);
                mState = FM_NONE;
            }
        });

        //Database
        Database.init(this);

        //Day Manager
        DateManager.init(this);

        //day list
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mDayList.setLayoutManager(mLayoutManager);
        mDayList.setHasFixedSize(true);
        mDayList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                View child = mDayList.getChildAt(0);
                if(recyclerView.getChildAdapterPosition(child) == 0) {
                    float y = child.getY();
                    onScroll(mFixedFirstItemPosition - y);
                    Log.d("TAG", " ---- " + (mFixedFirstItemPosition - y));
                }
                super.onScrolled(recyclerView, dx, dy);
            }

        });

        //ivEdit
        mIvEdit.setOnClickListener(v -> {
            if(!mHeaderVisible || mBlockButtons) return;
            blockButtons(800);
            if(mState == FM_NONE) {
                hideToCourseSettings();
            } else {
                long delay = mClassSettingsFragment.hide(0);
                showFromCourseSettings(delay);
            }
        });

        //loading view
        mLoadingView.setAlpha(0);

        makeInvisible();

        /*
        ...tatsächlich wird hier einfach die Methode <code>login()</code> ausgeführt...dies passiert nur 100 ms später
        Das erscheint Ihnen vielleicht sinnlos, hat aber einen Zweck
         */
        mHandler.postDelayed(() -> login(0), 100);
    }

    /**
     * Macht die Komponenten des Hauptbildschirms unsichtbar
     */
    private void makeInvisible() {
        mIvTitle.setVisibility(View.INVISIBLE);
        mIvEdit.setVisibility(View.INVISIBLE);
        mLyClass.setVisibility(View.INVISIBLE);
        mDayList.setVisibility(View.INVISIBLE);
    }

    /**
     * Wird aufgerufen, wenn die Liste gescrollt wird
     * @param distance Wie weit die Liste gerade vom Ursprungszustand(ungescrollter Zustand) entfernt ist
     */
    private void onScroll(float distance) {
        if(!mHeaderFadeEnabled) return;
        if(distance == 0) {
            showHeader();
        } else {
            hideHeader();
        }
    }

    /**
     * Vestecken aller Komponenten über der Liste (wird aufgerufen wenn die Scrolldistanz der Liste größer als 0 ist)
     */
    private void hideHeader() {
        if(!mHeaderVisible) return;
        mHeaderVisible = false;
        long delay = 0;

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
        mIvTitle.animate()
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

    /**
     * Anzeigen aller Komponenten über der Liste (wird aufgerufen wenn die Scrolldistanz der Liste gleich 0 ist)
     */
    private void showHeader() {
        if(mHeaderVisible) return;
        mHeaderVisible = true;
        long delay = 0;

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
        mIvTitle.animate()
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

    /**
     * Versuchen sich einzulogen
     * @param delay Zeitverzögerung
     */
    private void login(long delay) {
        showLoadingView(delay);
        delay += 300;

        //login and internet check
        mHandler.postDelayed(() -> {
            boolean isOnline = Utils.isOnline(this);
            if(isOnline) {
                //Database
                AuthManager.checkLogin(this, success -> { //Callback von AuthManager
                    if(success) {
                        startLoading(0);
                    } else {
                        if(Utils.isOnline(this)) {
                            hideLoadingView(0);
                            mState = FM_LOGIN;
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

    /**
     * Wenn nötig: Anzeigen der Kurswahl (beim ersten Start)
     * Database sagen, dass die VP-Daten heruntergeladen werden können
     *
     * @param delay Zeitverzögerung
     */
    public void startLoading(long delay) {
        mState = FM_NONE;

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

    /**
     * Wird aufgerufen, wenn der Thread für das Downloaden und Lesen der VP-Daten fertig ist
     * Befüllt bei Erfolg die Liste und zeigt die Hauptkomonenten an
     * @param ioException ob ein Lesefehler aufgetreten ist
     * @param otherException ob ein anderer Fehler aufgetreten ist
     * @param internetCut ob die Internetverbindung abgebrochen ist
     */
    public void onDatabaseLoaded(boolean ioException, boolean otherException, boolean internetCut) {
        if(!ioException && !otherException && !internetCut) {
            Database.loaded = true;
            hideLoadingView(0);

            if(!isPaused) {
                mHandler.postDelayed(() -> {
                    fillList(() -> enterMainComponents(0));
                }, 300);
            }

        } else if(ioException) {
            errorOnLoading(ErrorFragment.ERR_IO_EXCEPTION);
        } else if(otherException) {
            errorOnLoading(ErrorFragment.ERR_OTHER_EXCEPTION);
        } else {
            errorOnLoading(ErrorFragment.ERR_NO_INTERNET);
        }
    }

    /**
     * Füllen der Liste
     * @param runnable Runnable, was nach der Befüllung der Liste aufgerufen werden soll
     */
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

    /**
     * Zeigt einen Fehler an
     * @param errorCode Code für die Art des Fehlers
     */
    private void errorOnLoading(int errorCode) {
        long delay = 0;
        hideLoadingView(0);

        delay += 400;
        mErrorFragment.setError(errorCode);
        mState = FM_ERROR;
        mErrorFragment.show(delay);
    }

    /**
     * Wird nur beim Start ausgeführt.
     * Zeigt alle Haupt-Komponenten an.
     * @param delay Zeitverzögerund
     */
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
        mIvTitle.setVisibility(View.VISIBLE);
        mIvTitle.setAlpha(0f);
        mIvTitle.setTranslationY(50);
        mIvTitle.animate()
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
            mLyNothing.setVisibility(View.VISIBLE);
            mLyNothing.setAlpha(0f);
            mLyNothing.setTranslationY(50);
            mLyNothing.animate()
                    .setStartDelay(delay)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .translationY(0)
                    .alpha(1)
                    .start();
        } else {
            mLyNothing.setVisibility(View.INVISIBLE);
        }

        //list
        delay += 50;
        mDayList.setVisibility(View.VISIBLE);
        mDayListAdapter.show(delay);
    }

    /**
     * Versteckt alle Hauptkomponenten und zeigt die Kurswahl an
     */
    private void hideToCourseSettings() {
        long delay = 0;

        mState = FM_COURSE;

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
        mIvTitle.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //tv nothing
        if(mLyNothing.getVisibility() == View.VISIBLE) {
            mLyNothing.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(0)
                    .start();
        }

        //daylist
        if(mDayListAdapter != null) mDayListAdapter.hide(delay);

        delay += 200;

        mClassSettingsFragment.show(delay);
    }

    /**
     * Wird aufgerufen wenn die Kurswahl geschlossen wurde und die Haupt-Komponenten wieder angezeigt
     * werden sollen
     * @param delay Zeitverzögerung
     */
    private void showFromCourseSettings(long delay) {

        mState = FM_NONE;
        updateClassText();

        //avd animation
        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.avd_close_to_filter, getTheme());
        mIvEdit.setImageDrawable(drawable);
        drawable.start();

        //title
        delay += 50;
        mIvTitle.setTranslationY(50);
        mIvTitle.animate()
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
            mLyNothing.setVisibility(View.VISIBLE);
            mLyNothing.setTranslationY(50);
            mLyNothing.setAlpha(0);
            mLyNothing.animate()
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

    /**
     * Aktuallisieren der Klassenanzeige
     */
    private void updateClassText() {
        if(Integer.parseInt(Database.courseDegree) > 10) {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree));
        } else {
            mTvClass.setText(getString(R.string.temp_class, Database.courseDegree + "/" + Database.courseNumber));
        }
    }

    /**
     * Anzeigen des Ladekreises
     * @param delay Zeitverzögerung
     */
    private void showLoadingView(long delay) {
        mState = LOADING;
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300)
                .alpha(1)
                .start();
    }

    /**
     * Verstecken des Ladekreises
     * @param delay Zeitverzögerung
     */
    private void hideLoadingView(long delay) {
        mState = FM_NONE;
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

    /**
     * Wird aufgerufen wenn die Zurück-Taste des Smartphones gedrückt wurde
     * Schließt entweder die App oder geht einen Bildschirm zurück...
     */
    @Override
    public void onBackPressed() {
        switch(mState) { //Man konnte hier auch ein IF verwenden, aber es kommt später vieleicht nochwas dazu
            case FM_COURSE:
                if(!Database.courseChosen) {
                    super.onBackPressed();
                    return;
                }
                long delay = mClassSettingsFragment.hide(0);
                showFromCourseSettings(delay);
                break;
            default:
                super.onBackPressed();
        }
    }

    /**
     * Nehmen wir an die App wird durch den Home-Button in den "Stand-By" Modus versetzt und dann wieder geöffnet.
     * Die App wird dann nicht komplett neu gestartet, sondern es wird nur diese Methode aufgerufen.
     * Da Fragmente dabei nicht angezeigt werden, wird das eigentlich geöffnete Fragment wieder angezeigt.
     */
    @Override
    protected void onResume() {
        super.onResume();
        //hide statusbar
        View decorView = getWindow().getDecorView();  //Versteckt die Statusbar(Da wo Uhrzeigt, Akkuanzeige usw. steht)
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

//        mDayList.scrollToPosition(0);
        if(isPaused) {
            mHandler.postDelayed(() -> {
//            if(mDayList.getChildCount() != 0) mFixedFirstItemPosition = mDayList.getChildAt(0).getY();
                switch(mState) {
                    case LOADING:
                        break;
                    case FM_COURSE:
                        mClassSettingsFragment.show(0);
                        break;
                    case FM_LOGIN:
                        mLoginFragment.show(0);
                        break;
                    case FM_ERROR:
                        mErrorFragment.show(0);
                        break;
                    case FM_NONE:
                        mHandler.post(() -> {
                            fillList(() -> enterMainComponents(0));
                        });
                    /*if(mDayList.getChildCount() == 0) {
                        mLyNothing.setAlpha(1);
                    } else {
                        mLyNothing.setAlpha(0);
                    }
                    //ly class
                    mLyClass.setTranslationY(0);
                    mLyClass.setAlpha(1);

                    //title
                    mIvTitle.setTranslationY(0);
                    mIvTitle.setAlpha(1f);

                    //edit
                    mIvEdit.setTranslationY(0);
                    mIvEdit.setAlpha(1f);*/
                }
            }, 1000);
        }

        //load database
        Database.load();

        isPaused = false;
    }

    /**
     * Wird aufgerufen, wenn die App geschlossen oder pausiert wird
     */
    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        Database.save();
    }

    private void blockButtons(long duration) {
        mBlockButtons = true;
        mHandler.postDelayed(() -> mBlockButtons = false, duration);
    }
}
