package com.titaniel.bvcvertretungsplan.main_activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.authentication.AuthManager;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.date_manager.DateManager;
import com.titaniel.bvcvertretungsplan.fragments.AnimatedFragment;
import com.titaniel.bvcvertretungsplan.fragments.class_settings_fragment.ClassSettingsFragment;
import com.titaniel.bvcvertretungsplan.fragments.error_fragment.ErrorFragment;
import com.titaniel.bvcvertretungsplan.fragments.login_fragment.LoginFragment;
import com.titaniel.bvcvertretungsplan.fragments.substitute_plan_fragment.SubstitutePlanFragment;
import com.titaniel.bvcvertretungsplan.utils.Utils;
import com.victor.loading.rotate.RotateLoading;

import static com.titaniel.bvcvertretungsplan.connection_result.ConnectionResult.RES_NO_INTERNET;
import static com.titaniel.bvcvertretungsplan.connection_result.ConnectionResult.RES_SERVER_DOWN;
import static com.titaniel.bvcvertretungsplan.connection_result.ConnectionResult.RES_SUCCESS;
import static com.titaniel.bvcvertretungsplan.connection_result.ConnectionResult.RES_WRONG_LOGIN;

/**
 * Repräsentiert das Hauptfenster, also den Frame, der den ganzen Bildschirm ausfüllt.
 * Hier befindet sich die Anzeige des Vertretungsplans, es wird jedoch auch die Anzeige anderer
 * Fragmente(zum Beispiel ErrorFragment oder LoginFragment) geregelt
 */
public class MainActivity extends AppCompatActivity {

    //states
    public static final int
            STATE_FM_SUBSTITUTE = 0,
            STATE_FM_CLASS_SETTINGS = 1,
            STATE_FM_LOGIN = 2,
            STATE_FM_ERROR = 3,
            STATE_LOADING = 4;

    public int state = STATE_LOADING;
    private boolean mIsPaused = false, mSubstituteShowBlockedBecauseOfPause = false;

    private ImageView mIvBackground;
    private View mVBgOverlay;
    private RotateLoading mLoadingView;

    public SubstitutePlanFragment substitutePlanFragment;
    public ErrorFragment errorFragment;
    public ClassSettingsFragment classSettingsFragment;
    public LoginFragment loginFragment;

    private Handler mHandler = new Handler();

    /**
     * Hier ist der Start-Punkt der ganzen Anwendung. Hier werden alle Initialisierungen ausgeführt.
     *
     * @param savedInstanceState Android spezifisch... wird nicht genutzt deswegen erkläre ich das jetzt nicht :D
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        mLoadingView = findViewById(R.id.loadingView);
        mIvBackground = findViewById(R.id.ivBackground);
        mVBgOverlay = findViewById(R.id.vBackgroundOverlay);


        //init fragments
        substitutePlanFragment = (SubstitutePlanFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentSubstitute);
        errorFragment = (ErrorFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentErr);
        classSettingsFragment = (ClassSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentClassSettings);
        loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentLogin);

        //errorFragment
        errorFragment.setErrorFragmentCallback(new ErrorFragment.ErrorFragmentCallback() {
            @Override
            public void onBtnAgainClicked(Button button) {
                errorFragment.hide(0);
                login(300);
            }

            @Override
            public void onBtnOfflineClicked(Button button) {
                errorFragment.hide(0);
                Database.fetchData(MainActivity.this, true);
            }
        });

        //Database
        Database.init(this);

        //Day Manager
        DateManager.init(this);

        //loading view
        mLoadingView.start();
        mLoadingView.setAlpha(0);

        mHandler.postDelayed(() -> login(0), 100);
    }

    /**
     * Versuchen sich einzulogen
     *
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
                AuthManager.checkLogin(this, result -> { //Callback von AuthManager

                    if(result == RES_SUCCESS) {
                        load(0);
                    } else if(result == RES_SERVER_DOWN) {
                        showErrorFragment(0, RES_SERVER_DOWN, null);
                    } else if(result == RES_WRONG_LOGIN && Utils.isOnline(this)) {
                        showLoginFragment(0, null);
                    } else {
                        showErrorFragment(0, RES_NO_INTERNET, null);
                    }

                });
            } else {
                showErrorFragment(0, RES_NO_INTERNET, null);
            }

        }, delay);

    }

    /**
     * Wenn nötig: Anzeigen der Kurswahl (beim ersten Start)
     * Database sagen, dass die VP-Daten heruntergeladen werden können
     *
     * @param delay Zeitverzögerung
     */
    public void load(long delay) {
        if(!Database.classChosen) {
            mHandler.postDelayed(() -> {
                showClassSettingsFragment(0, null);
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
                showErrorFragment(0, RES_NO_INTERNET, null);
            }

        }, delay);
    }

    /**
     * Wird aufgerufen, wenn der Thread für das Downloaden und Lesen der VP-Daten fertig ist
     * Befüllt bei Erfolg die Liste und zeigt die Hauptkomonenten an
     *
     * @param resultCode Ergebnis
     */
    public void onLoaded(int resultCode) {
        if(resultCode == RES_SUCCESS) {
            Database.loaded = true;
            hideLoadingView(0);

            if(!mIsPaused) {
                mHandler.postDelayed(() -> {
                    showSubstituteFragment(0, true, null);
                }, 300);
            } else {
                mSubstituteShowBlockedBecauseOfPause = true;
            }

        } else {
            showErrorFragment(0, resultCode, null);
        }
    }

    /**
     * Zeigt einen Fehler an
     *
     * @param errorCode Code für die Art des Fehlers
     */
    public void showErrorFragment(long delay, int errorCode, @Nullable AnimatedFragment oldFragment) {
        state = STATE_FM_ERROR;
        delay += hideLoadingView(delay);
        if(oldFragment != null) delay += oldFragment.hide(delay);
        if(errorCode != -1) errorFragment.setError(errorCode);
        errorFragment.show(delay);
    }

    public void showLoginFragment(long delay, @Nullable AnimatedFragment oldFragment) {
        state = STATE_FM_LOGIN;
        delay += hideLoadingView(delay);
        if(oldFragment != null) delay += oldFragment.hide(delay);
        loginFragment.show(delay);
    }

    public void showClassSettingsFragment(long delay, @Nullable AnimatedFragment oldFragment) {
        state = STATE_FM_CLASS_SETTINGS;
        delay += hideLoadingView(delay);
        if(oldFragment != null) delay += oldFragment.hide(delay);
        classSettingsFragment.show(delay);
    }

    public void showSubstituteFragment(long delay, boolean updateSubstitutes, @Nullable AnimatedFragment oldFragment) {
        delay += hideLoadingView(delay);

        final long d = delay;

        if(updateSubstitutes) {
            substitutePlanFragment.fillList(() -> {
                long aDelay = 0;
                if(oldFragment != null) aDelay += oldFragment.hide(d);
                state = STATE_FM_SUBSTITUTE;
                substitutePlanFragment.show(d + aDelay);
                //Background overlay
                mVBgOverlay.animate()
                        .setStartDelay(0)
                        .setDuration(500)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .alpha(0)
                        .start();
            });
        } else {
            state = STATE_FM_SUBSTITUTE;
            if(oldFragment != null) delay += oldFragment.hide(delay);
            substitutePlanFragment.show(delay);
        }
    }

    /**
     * Anzeigen des Ladekreises
     *
     * @param delay Zeitverzögerung
     */
    private void showLoadingView(long delay) {
        state = STATE_LOADING;
        if(mLoadingView.getVisibility() == View.VISIBLE) return;
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
     *
     * @param delay Zeitverzögerung
     */
    private long hideLoadingView(long delay) {
        if(mLoadingView.getVisibility() == View.GONE) return 0;
        mLoadingView.animate()
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(300)
                .alpha(0)
                .withEndAction(() -> mLoadingView.setVisibility(View.GONE))
                .start();
        return 400;
    }

    /**
     * Wird aufgerufen wenn die Zurück-Taste des Smartphones gedrückt wurde
     * Schließt entweder die App oder geht einen Bildschirm zurück...
     */
    @Override
    public void onBackPressed() {
        switch(state) {
            case STATE_FM_CLASS_SETTINGS:
                if(!Database.classChosen) {
                    super.onBackPressed();
                    return;
                }
//                long delay = classSettingsFragment.hide(0);
                showSubstituteFragment(0, false, classSettingsFragment);
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
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        if(mIsPaused) {
            mHandler.postDelayed(() -> {
                if(mSubstituteShowBlockedBecauseOfPause) {
                    showSubstituteFragment(0, true, null);
                    mSubstituteShowBlockedBecauseOfPause = false;
                }
//
//                switch(state) {
//                    case STATE_LOADING:
//                        break;
//                    case STATE_FM_CLASS_SETTINGS:
//                        showClassSettingsFragment(0, null);
//                        break;
//                    case STATE_FM_LOGIN:
//                        showLoginFragment(0, null);
//                        break;
//                    case STATE_FM_ERROR:
//                        showErrorFragment(0, -1, null);
//                        break;
//                    case STATE_FM_SUBSTITUTE:
////                        if(!mSubstituteShownOnes) {
////                        }
//                }
            }, 1000);
        }

        //load database
        Database.load();
        mIsPaused = false;
    }

    /**
     * Wird aufgerufen, wenn die App geschlossen oder pausiert wird
     */
    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
        Database.save();
    }
}
