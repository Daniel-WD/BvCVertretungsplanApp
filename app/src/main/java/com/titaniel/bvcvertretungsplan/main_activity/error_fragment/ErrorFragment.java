package com.titaniel.bvcvertretungsplan.main_activity.error_fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.util.Random;

/**
 * @author Daniel Weidensdörfer
 * Fragment, welches dem Nutzer Fehler mitteilt(zum Beispiel ein fehlende Internet Verbindung)
 */
public class ErrorFragment extends Fragment {

    public static final int ERR_NO_INTERNET = 0;
    public static final int ERR_IO_EXCEPTION = 1;
    public static final int ERR_OTHER_EXCEPTION = 2;

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
        return inflater.inflate(R.layout.fragment_error, container, false);
    }

    /**
     * Von Android aufgerufen wenn das Fragment startet
     *
     * Enthält die Initialisierungen
     */
    @Override
    public void onStart() {
        super.onStart();

        //init
        mRoot = getView();
        mIvErr = mRoot.findViewById(R.id.ivErr);
        mTvErr = mRoot.findViewById(R.id.tvErr);
        mTvTitle = mRoot.findViewById(R.id.ivTit);
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

        //make invisible
        mRoot.setVisibility(View.INVISIBLE);
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
        int accentColor = ContextCompat.getColor(getContext(), R.color.accent);
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
    }

    /**
     * Zeigt das Fragment an
     * @param delay Zeitverzögerung
     */
    public void show(long delay) {
        Random r = new Random();
        mIvErr.setImageResource(r.nextInt(2) == 0 ? R.drawable.ic_emj_angry : R.drawable.ic_emj_sad);
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
    public void hide(long delay) {
        mRoot.animate()
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200)
                .alpha(0)
                .translationY(100)
                .withEndAction(() -> mRoot.setVisibility(View.INVISIBLE))
                .start();
    }

}
