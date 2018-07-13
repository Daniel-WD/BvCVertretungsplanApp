package com.titaniel.bvcvertretungsplan.fragments.error_fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.fragments.AnimatedFragment;
import com.titaniel.bvcvertretungsplan.main_activity.MainActivity;

import java.util.Random;

import static com.titaniel.bvcvertretungsplan.connection_result.ConnectionResult.*;

/**
 * @author Daniel Weidensdörfer
 * Fragment, welches dem Nutzer Fehler mitteilt
 */
public class ErrorFragment extends AnimatedFragment {

    /**
     * Callback...wenn ein Button dieses Fragments geklickt wurde
     */
    public interface ErrorFragmentCallback {
        void onBtnAgainClicked(Button button);
        void onBtnOfflineClicked(Button button);
    }

    private ErrorFragmentCallback mErrorFragmentCallback = null;

    private View mRoot;
    private Button mBtnErrAgain, mBtnErrOffline;
    private ImageView mIvErr;
    private TextView mTvErr;
    private TextView mTvTitle;

    private int mAccentColor;

    private MainActivity mActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_error, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mActivity = (MainActivity) getActivity();

        mAccentColor = ContextCompat.getColor(getContext(), R.color.accent);

        //init
        mRoot = getView();
        mIvErr = mRoot.findViewById(R.id.ivErr);
        mTvErr = mRoot.findViewById(R.id.tvErr);
        mTvTitle = mRoot.findViewById(R.id.tvErrorTitle);
        mBtnErrAgain = mRoot.findViewById(R.id.btnErrAgain);
        mBtnErrOffline = mRoot.findViewById(R.id.btnErrOffline);

        //btn err again
        mBtnErrAgain.setOnClickListener(v -> {
            mErrorFragmentCallback.onBtnAgainClicked((Button) v);
        });

        //btn err offline
        mBtnErrOffline.setOnClickListener(v -> {
            mErrorFragmentCallback.onBtnOfflineClicked((Button) v);
        });
    }

    /**
     * Setzt das Callback
     * @param callback Callback
     */
    public void setErrorFragmentCallback(ErrorFragmentCallback callback) {
        mErrorFragmentCallback = callback;
    }

    /**
     * Setzt welche Fehlermeldung angezeigt werden soll
     * @param errorCode Fehler-Art
     */
    public void setError(int errorCode) {
        switch(errorCode) {
            case RES_ITS_HOLIDAYS:
                confUI(getString(R.string.holi), getString(R.string.err_holidays),
                        false, true,
                        -1, mAccentColor);
                break;

            case RES_SERVER_DOWN:
            case RES_IO_EXCEPTION:

                confUI(getString(R.string.omg), getString(R.string.err_io_exception),
                        false, true,
                        -1, mAccentColor);
                break;

            case RES_NO_INTERNET:
                if(Database.classChosen) {
                    confUI(getString(R.string.omg), getString(R.string.err_no_internet),
                            true, true,
                            mAccentColor, Color.WHITE);

                } else {
                    confUI(getString(R.string.omg), getString(R.string.err_no_internet),
                            false, true,
                            -1, mAccentColor);
                }
                break;

            case RES_XML_EXCEPTION:
                confUI(getString(R.string.omg), getString(R.string.err_other_exception),
                        false, true,
                        -1, mAccentColor);
                break;
        }
    }

    private void confUI(String title, String text,
                        boolean btnOfflineVisible, boolean btnAgainVisible) {
        confUI(title, text, btnOfflineVisible, btnAgainVisible, -1, -1);
    }

    private void confUI(String title, String text,
                        boolean btnOfflineVisible, boolean btnAgainVisible,
                        int btnOfflineColor, int btnAgainColor) {
        mTvTitle.setText(title);
        mTvErr.setText(text);
        mBtnErrOffline.setVisibility(btnOfflineVisible ? View.VISIBLE : View.GONE);
        mBtnErrAgain.setVisibility(btnAgainVisible ? View.VISIBLE : View.GONE);
        if(btnOfflineColor != -1) mBtnErrOffline.setTextColor(btnOfflineColor);
        if(btnAgainColor != -1) mBtnErrAgain.setTextColor(btnAgainColor);
    }

    /**
     * Zeigt das Fragment an
     * @param delay Zeitverzögerung
     */
    public void animateShow(long delay) {
        boolean isHoliday = true;

        if(isHoliday) {
            mIvErr.setImageResource(R.drawable.ic_emj_admired);
        } else {
            Random r = new Random();
            mIvErr.setImageResource(r.nextInt(2) == 0 ? R.drawable.ic_emj_angry : R.drawable.ic_emj_sad);
        }

        mRoot.setVisibility(View.VISIBLE);
        mRoot.setTranslationY(-100);
        mRoot.setAlpha(0);
        mRoot.animate()
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(200)
                .alpha(1)
                .translationY(0)
                .start();
    }

    /**
     * Versteckt das Fragment
     * @param delay Zeitverzögerung
     */
    public long animateHide(long delay) {
        mRoot.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200)
                .alpha(0)
                .translationY(100)
                .withEndAction(() -> mRoot.setVisibility(View.INVISIBLE))
                .start();
        return 200;
    }

}
