package com.titaniel.bvcvertretungsplan.fragments.login_fragment;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.authentication.AuthManager;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.fragments.AnimatedFragment;
import com.titaniel.bvcvertretungsplan.main_activity.MainActivity;
import com.titaniel.bvcvertretungsplan.utils.Utils;

import static com.titaniel.bvcvertretungsplan.connection_result.ConnectionResult.*;

/**
 * @author Daniel Weidensdörfer
 * Fragment für das Login
 */
public class LoginFragment extends AnimatedFragment {

    private View mRoot;
    private ImageView mIvIcon;
    private View mDivider;
    private TextView mTvLogin;
    private EditText mTUser, mTPassword;
    private ImageView mIvUser, mIvPassword;
    private Button mBtnOk;
    private ProgressBar mProgressBar;
    private TextView mTvInfo;
    private FrameLayout mLyUser, mLyPassword;
    private LinearLayout mLyLogin;
    private TextView mTvTitle;

    private int colorFocused, colorNormal = Color.WHITE;

    private Handler mHandler = new Handler();

    private MainActivity mActivity;

    /**
     * Von Android aufgerufen wenn die <code>View</code> erstellt werden soll
     * Liefert die <code>View</code> zurück
     *
     * @param inflater LayoutInflater
     * @param container Container
     * @param savedInstanceState SavedInstanceState
     * @return View
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    /**
     * Von Android aufgerufen wenn das Fragment startet
     *
     * Enthält die Initialisierungen
     */
    @Override
    public void onStart() {
        super.onStart();

        mActivity = (MainActivity) getActivity();

        colorNormal = ContextCompat.getColor(getContext(), R.color.loginIcon);
        colorFocused = ContextCompat.getColor(getContext(), R.color.loginIconFocused);

        //init
        mRoot = getView();
        mIvIcon = mRoot.findViewById(R.id.ivIcon);
        mDivider = mRoot.findViewById(R.id.dividerTop);
        mTvLogin = mRoot.findViewById(R.id.tvLogin);
        mTUser = mRoot.findViewById(R.id.tUser);
        mTPassword = mRoot.findViewById(R.id.tPassword);
        mIvUser = mRoot.findViewById(R.id.ivUser);
        mIvPassword = mRoot.findViewById(R.id.ivPassword);
        mBtnOk = mRoot.findViewById(R.id.btnOk);
        mProgressBar = mRoot.findViewById(R.id.progressBar);
        mTvInfo = mRoot.findViewById(R.id.tvInfo);
        mLyUser = mRoot.findViewById(R.id.lyUser);
        mLyPassword = mRoot.findViewById(R.id.lyPassword);
        mLyLogin = mRoot.findViewById(R.id.lyLoginText);
        mTvTitle = mRoot.findViewById(R.id.tvLoginTitle);

        //fail text
        mTvInfo.setAlpha(0);

        //edit texts
        mTUser.setOnFocusChangeListener((v, hasFocus) -> {
            if(mTUser.getText().length() != 0) return;
            changeIvColor(mIvUser, hasFocus);
        });
        mTPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if(mTPassword.getText().length() != 0) return;
            changeIvColor(mIvPassword, hasFocus);
        });

        //btn
        mBtnOk.setOnClickListener(v -> {
            hideFail(0);
            mHandler.postDelayed(() -> {
                if(Utils.isOnline(getContext())) {
                    String user = mTUser.getText().toString().trim();
                    String pw = mTPassword.getText().toString().trim();
                    AuthManager.checkLogin(getContext(), user, pw, (result) -> {
                        if(result == RES_SUCCESS) {
                            Database.username = mTUser.getText().toString().trim();
                            Database.password = mTPassword.getText().toString().trim();
                            hide(0);
                        } else {
                            hideLoadingBar();
                            mTvInfo.setText(R.string.fail);
                            showFail(300);
                            mTPassword.setText("");
                            mTUser.setText("");
                            mTUser.requestFocus();
                        }
                    });
                } else {
                    mTvInfo.setText(R.string.fail_offline);
                    hideLoadingBar();
                    showFail(300);
                }
            }, 1000);

            showLoadingBar();
        });
    }

    /**
     * Anzeigen des Texts, welcher einen Fehler enthält(Falsches Login oder keine Internet Verbindung)
     * @param delay Zeitverzögerung
     */
    private void showFail(long delay) {
        mTvInfo.setText(R.string.fail);
        mTvInfo.setVisibility(View.VISIBLE);
        mTvInfo.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .translationY(0)
                .alpha(1);
    }

    /**
     * Verstecken des Fehler Texts
     * @param delay Zeitverzögerung
     */
    private void hideFail(long delay) {
        mTvInfo.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0);
    }

    /**
     * Versteckt den OK Button und zeigt die Lade Anzeige an
     */
    private void showLoadingBar() {
        mTUser.setEnabled(false);
        mTPassword.setEnabled(false);
        long delay = 0;
        mBtnOk.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new AnticipateInterpolator(2f))
                .scaleY(0)
                .scaleX(0)
                .alpha(0)
                .start();
        delay += 250;
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setScaleX(0);
        mProgressBar.setScaleY(0);
        mProgressBar.setAlpha(0);
        mProgressBar.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator(1f))
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .start();
    }

    /**
     * Zeigt den OK Button und versteckt die Lade Anzeige
     */
    private void hideLoadingBar() {
        mTUser.setEnabled(true);
        mTPassword.setEnabled(true);
        long delay = 0;
        mProgressBar.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new AnticipateInterpolator(2f))
                .scaleX(0)
                .scaleY(0)
                .alpha(0)
                .start();
        delay += 250;
        mBtnOk.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator(1f))
                .scaleY(1)
                .scaleX(1)
                .alpha(1)
                .start();
    }

    /**
     * Anzeige dieses Fragments
     * @param delay Zeitverzögerung
     */
    public void animateShow(long delay) {
        mRoot.setVisibility(View.VISIBLE);

        //icon
        mIvIcon.setVisibility(View.VISIBLE);
        mIvIcon.setAlpha(0f);
        mIvIcon.setTranslationY(30);
        mIvIcon.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //title
        delay += 50;
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setAlpha(0f);
        mTvTitle.setTranslationY(30);
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //login plus its line
        delay += 50;
        mLyLogin.setVisibility(View.VISIBLE);
        mLyLogin.setAlpha(0f);
        mLyLogin.setTranslationY(30);
        mLyLogin.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //userlayout
        delay += 50;
        mLyUser.setVisibility(View.VISIBLE);
        mLyUser.setAlpha(0f);
        mLyUser.setTranslationY(30);
        mLyUser.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //passwordlayout
        delay += 50;
        mLyPassword.setVisibility(View.VISIBLE);
        mLyPassword.setAlpha(0f);
        mLyPassword.setTranslationY(30);
        mLyPassword.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //btn ok
        delay += 50;
        mBtnOk.setVisibility(View.VISIBLE);
        mBtnOk.setAlpha(0f);
        mBtnOk.setTranslationY(30);
        mBtnOk.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        delay += 400;

        mTvInfo.setVisibility(View.VISIBLE);
        mTvInfo.setAlpha(0f);
        mTvInfo.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

    }

    /**
     * Verstecken dieses Fragments
     * @param delay Zeitverzögerung
     */
    public long animateHide(long delay) {
        mRoot.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .withEndAction(() -> mRoot.setVisibility(View.INVISIBLE))
                .start();

        delay += 300;

        mActivity.load(delay);

        return 300;
    }

    /**
     * Beim Login gibt es links bei den Eingabefeldern supergeile Icons...
     * Wenn etwas in dem Eingabefeld steht ist das jeweilige Icon vollständig weiß... ansonsten ist es gräulich
     *
     * Die Methode ändert also die Farbe des Bildes einer ImageView(View in der ein Bild eingezeigt wird)
     *
     * @param iv ImageView
     * @param focus true wenn das Icon weiß werden soll, ansonsten wird es gräulich
     */
    private void changeIvColor(ImageView iv, boolean focus) {
        int colorFrom, colorTo;
        if(focus) {
            colorFrom = colorNormal;
            colorTo = colorFocused;
        } else {
            colorFrom = colorFocused;
            colorTo = colorNormal;
        }
        ValueAnimator colorAnim = ValueAnimator.ofArgb(colorFrom, colorTo);
        colorAnim.addUpdateListener((animation) -> {
            iv.setColorFilter((Integer) animation.getAnimatedValue());
        });
        colorAnim.setDuration(200);
        colorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnim.start();
    }
}
