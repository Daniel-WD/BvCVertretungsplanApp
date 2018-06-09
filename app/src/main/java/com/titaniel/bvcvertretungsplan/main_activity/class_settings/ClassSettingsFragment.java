package com.titaniel.bvcvertretungsplan.main_activity.class_settings;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.database.Database;
import com.titaniel.bvcvertretungsplan.main_activity.MainActivity;
import com.titaniel.bvcvertretungsplan.main_activity.class_settings.course_picker.CoursePickerFragment;
import com.titaniel.bvcvertretungsplan.utils.DateManager;

import java.util.ArrayList;

/**
 * Fragment für die Klassenwahl
 */
public class ClassSettingsFragment extends Fragment {

    private static final String[] sDegrees = {"5", "6", "7", "8", "9", "10", "11", "12"};
    private static final String[] sNumbers = {"1", "2", "3", "4", "5", "6"};

    private MainActivity mActivity;

    private View mRoot;
    private RecyclerView mCourseDegreeList, mCourseNumberList;
    private TextView mTvDegree, mTvNumber;
    private Button mBtnBack, mBtnOk, mBtnCourses, mBtnSkipCourses;
    private TextView mTvTitle;
    private RelativeLayout mLyClass;
    private LinearLayout mLyButtons;
    private View mBackground;

    private Handler mHandler = new Handler();

    private CoursePickerFragment mCoursePickerFragment;

    private boolean mButtonsBlocked = false;

    /**
     * Von Android aufgerufen, wenn die <code>View</code> erstellt werden soll
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
        return inflater.inflate(R.layout.fragment_class_settings, container, false);
    }

    /**
     * Von Android aufgerufen wenn das Fragment startet
     *
     * Enthält die Initialisierungen
     */
    @Override
    public void onStart() {
        super.onStart();

        mActivity = (MainActivity) getContext();

        //fragments
        mCoursePickerFragment = (CoursePickerFragment) getChildFragmentManager().findFragmentById(R.id.fragmentCoursePicker);

        //init
        mRoot = getView();
        mCourseDegreeList = mRoot.findViewById(R.id.courseDegreeList);
        mCourseNumberList = mRoot.findViewById(R.id.courseNumberList);
        mTvDegree = mRoot.findViewById(R.id.tvCourseDegree);
        mTvNumber = mRoot.findViewById(R.id.tvCourseNumber);
        mBtnBack = mRoot.findViewById(R.id.btnBack);
        mBtnOk = mRoot.findViewById(R.id.btnOk);
        mBtnCourses = mRoot.findViewById(R.id.btnCourses);
        mTvTitle = mRoot.findViewById(R.id.tvChooseClass);
        mLyClass = mRoot.findViewById(R.id.lyClass);
        mLyButtons = mRoot.findViewById(R.id.lyButtons);
        mBackground = mRoot.findViewById(R.id.vBackground);
        mBtnSkipCourses = mRoot.findViewById(R.id.btnSkipCourses);

        //btn skip courses
        mBtnSkipCourses.setOnClickListener((v) -> {
            if(mButtonsBlocked) return;
            Database.selectedCourses = new ArrayList<>();
            mBtnOk.callOnClick();
        });

        //btn back
        mBtnBack.setOnClickListener((v) -> {
            if(mButtonsBlocked) return;
            blockButtons(2000);
            mActivity.onBackPressed();
        });

        //btn Ok
        mBtnOk.setOnClickListener((v) -> {
            if(mButtonsBlocked) return;
            blockButtons(2000);
            String newDegree = mTvDegree.toString();
            String newNumber = mTvNumber.toString();
            if(!Database.courseNumber.equals(newNumber) || !Database.courseDegree.equals(newDegree) || !Database.courseChosen) {
                confirmChangesAndHide();
            } else {
                mActivity.onBackPressed();
            }
        });

        //btn courses
        mBtnCourses.setOnClickListener((v) -> {
            if(mButtonsBlocked) return;
            blockButtons(2000);
            long delay = hideToPicker(0);
            mCoursePickerFragment.show(delay);
        });

        //mCourseDegreeList
        LinearLayoutManager managerDegrees =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCourseDegreeList.setLayoutManager(managerDegrees);
        mCourseDegreeList.setHasFixedSize(true);
        mCourseDegreeList.setAdapter(new NumberAdapter(getContext(), sDegrees, R.layout.class_settings_item_degree, this::degreePickerClicked));

        //mCourseNumberList
        LinearLayoutManager managerNumber =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCourseNumberList.setLayoutManager(managerNumber);
        mCourseNumberList.setHasFixedSize(true);
        mCourseNumberList.setAdapter(new NumberAdapter(getContext(), sNumbers, R.layout.class_settings_item_number, this::numberPickerClicked));

        //mCourseNumberList.post(this::refreshListSelections);

        mRoot.setVisibility(View.INVISIBLE);
    }

    /**
     * Klasse bestätigen und wieder zur Hauptoberfläche zurückkehren
     */
    public void confirmChangesAndHide() {
        Database.courseDegree = mTvDegree.getText().toString();
        Database.courseNumber = mTvNumber.getText().toString();
        DateManager.prepare();
        if(Database.courseChosen) {
            mActivity.fillList(() -> {
                mActivity.onBackPressed();
            });
        } else {
            Database.courseChosen = true;
            long delay = hide(0);
            mActivity.startLoading(delay);
        }
    }

    /**
     * Zeigt Klassenwahl wieder an. Soll aufgerufen werden, wenn sich die Kurswahl schließt(überlicherweise nach einem Abbruch)
     * @param delay Zeitverzögerung
     */
    public void showFromPicker(long delay) {
        //title
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //class layout
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //list top
        mCourseDegreeList.setVisibility(View.VISIBLE);
        mCourseDegreeList.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //list bottom
        mCourseNumberList.setVisibility(View.VISIBLE);
        mCourseNumberList.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //ly buttons
        mLyButtons.setVisibility(View.VISIBLE);
        mLyButtons.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1)
                .start();

        //skip button
        if(mBtnCourses.getVisibility() == View.VISIBLE) {
            mBtnSkipCourses.setVisibility(View.VISIBLE);
            mBtnSkipCourses.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(0.6f)
                    .start();
        }
    }

    /**
     * Soll aufgerufen werden, wenn die Klassenwahl geschlossen wird und sich danach die Kurswahl öffnen soll
     * @param delay Zeitverzögerung
     * @return Zeitverzögerung für nächste Animation
     */
    public long hideToPicker(long delay) {
        //title
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //class layout
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        //list top
        mCourseDegreeList.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> mCourseDegreeList.setVisibility(View.INVISIBLE))
                .alpha(0)
                .start();

        //list bottom
        mCourseNumberList.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> mCourseNumberList.setVisibility(View.INVISIBLE))
                .alpha(0)
                .start();

        //list bottom
        mLyButtons.animate()
                .setStartDelay(delay)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> mLyButtons.setVisibility(View.INVISIBLE))
                .alpha(0)
                .start();

        //skip button
        if(mBtnCourses.getVisibility() == View.VISIBLE) {
            mBtnSkipCourses.animate()
                    .setStartDelay(delay)
                    .setDuration(150)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> mLyButtons.setVisibility(View.INVISIBLE))
                    .alpha(0)
                    .start();
        }

        delay += 200;

        return delay;
    }

    /**
     * Anzeige der Klassenwahl von der Hauptliste (wenn der Nutzer seine Klasse wählen will)
     * @param delay Zeitverzögerung
     */
    public void show(long delay) {
        mRoot.setVisibility(View.VISIBLE);
        mRoot.setAlpha(1);

        refreshListSelections();

        //background
        if(Database.courseChosen) {
            mBackground.setVisibility(View.VISIBLE);
            mBackground.setAlpha(0);
            mBackground.animate()
                    .setStartDelay(delay)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .start();
        } else {
            mBackground.setVisibility(View.INVISIBLE);
        }

        //title
        mTvTitle.setAlpha(0);
        mTvTitle.setTranslationY(50);
        mTvTitle.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //class layout
        delay += 50;
        mLyClass.setAlpha(0);
        mLyClass.setTranslationY(50);
        mLyClass.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //list top
        delay += 50;
        mCourseDegreeList.setVisibility(View.VISIBLE);
        mCourseDegreeList.setAlpha(0);
        mCourseDegreeList.setTranslationY(50);
        mCourseDegreeList.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //list bottom
        delay += 50;
        mCourseNumberList.setVisibility(View.VISIBLE);
        mCourseNumberList.setAlpha(0);
        mCourseNumberList.setTranslationY(50);
        mCourseNumberList.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //buttons
        delay += 50;
        if(Database.courseChosen) {
            mBtnBack.setVisibility(View.VISIBLE);
        } else {
            mBtnBack.setVisibility(View.GONE);
        }
        mLyButtons.setVisibility(View.VISIBLE);
        mLyButtons.setAlpha(0);
        mLyButtons.setTranslationY(50);
        mLyButtons.animate()
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1)
                .translationY(0)
                .start();

        //skip button
        if(mBtnCourses.getVisibility() == View.VISIBLE) {
            delay += 50;
            mBtnSkipCourses.setVisibility(View.VISIBLE);
            mBtnSkipCourses.setAlpha(0);
            mBtnSkipCourses.setTranslationY(50);
            mBtnSkipCourses.animate()
                    .setStartDelay(delay)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(0.6f)
                    .translationY(0)
                    .start();
        } else {
            mBtnSkipCourses.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Schließen der Klassenwahl (und der Kurswahl, wenn diese geöffnet ist)
     * @param delay Zeitverzögerung
     * @return Zeitverzögerung für nächste Animation
     */
    public long hide(long delay) {
        mCoursePickerFragment.hide(0);
        mRoot.animate()
                .setStartDelay(delay)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .withEndAction(() -> mRoot.setVisibility(View.INVISIBLE))
                .start();

        delay += 250;

        return delay;
    }

    /**
     * wird aufgerufen, wenn eine Klasse(5-12) gewählt wurde
     * Ändert die Klassenanzeige.
     * Macht die Klassennummerwahl nicht ansprechbar, wenn die Klasse 11 oder 12 gewählt wurde
     * @param number Klasse als String
     */
    private void degreePickerClicked(String number) {
        long delay = 0;
        mTvDegree.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();
        mTvNumber.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        delay += 100;
        mHandler.postDelayed(() -> {
            if(Integer.parseInt(number) > 10) {
                mTvNumber.setVisibility(View.GONE);
                switchOkCourses(false);
            } else {
                mTvNumber.setVisibility(View.VISIBLE);
                switchOkCourses(true);
            }
            mTvDegree.animate()
                    .setDuration(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .withStartAction(() -> {
                        mTvDegree.setText(number);
//                        mTvNumber.setText(Database.courseNumber);
                    })
                    .start();
            mTvNumber.animate()
                    .setDuration(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .alpha(1)
                    .start();
        }, delay);

        ((NumberAdapter) mCourseNumberList.getAdapter()).setEnabled(Integer.parseInt(number) <= 10);
    }

    /**
     * Wird aufgerufen, wenn die Klassen Nummer aufgerufen wurde
     *
     * Ändert die Klassenanzeige
     *
     * @param number Klassennumer
     */
    private void numberPickerClicked(String number) {
        long delay = 0;
        mTvNumber.animate()
                .setStartDelay(delay)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0)
                .start();

        delay += 100;
        mHandler.postDelayed(() -> {
            mTvNumber.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(100)
                    .withStartAction(() -> mTvNumber.setText(number))
                    .alpha(1)
                    .start();
        }, delay);
    }

    /**
     * Wechselt zwischen der Anzeige des OK Buttons und der Anzeige des KURSE Buttons
     * Wenn die Klassen 11 oder 12 gewählt wurden, dann wird KURSE angezeigt ansonsten ok
     *
     * Man beachte, dass die beiden Buttons unterschiedliche Buttons sind und nicht nur der Text
     * eines Buttons verändert wurde
     *
     * @param toOk true wenn zu OK gewechselt werden soll, false wenn zu KURSE gewechsel werden soll
     */
    private void switchOkCourses(boolean toOk) {
        float alphaOk, alphaCourses;
        if(toOk) {
            alphaOk = 1;
            alphaCourses = 0;
        } else {
            alphaOk = 0;
            alphaCourses = 1;
        }
        long dur = 150;
        if(alphaOk == 1) mBtnOk.setVisibility(View.VISIBLE);
        mBtnOk.animate()
                .setStartDelay(alphaOk == 0 ? 0 : dur)
                .setDuration(dur)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(alphaOk)
                .withEndAction(() -> {
                    if(alphaOk == 0) mBtnOk.setVisibility(View.INVISIBLE);
                })
                .start();
        if(alphaCourses == 1) {
            mBtnCourses.setVisibility(View.VISIBLE);
            mBtnSkipCourses.setVisibility(View.VISIBLE);
        }
        mBtnCourses.animate()
                .setStartDelay(alphaCourses == 0 ? 0 : dur)
                .setDuration(dur)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(alphaCourses)
                .withEndAction(() -> {
                    if(alphaCourses == 0) mBtnCourses.setVisibility(View.INVISIBLE);
                })
                .start();
        mBtnSkipCourses.animate()
                .setStartDelay(0)
                .setDuration(2*dur)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(alphaCourses == 1 ? 0.6f : 0)
                .withEndAction(() -> {
                    if(alphaCourses == 0) mBtnSkipCourses.setVisibility(View.INVISIBLE);
                })
                .start();
    }

    /**
     * Relevant beim Öffnen des Fragments.
     *
     * Nehmen wir an es ist gerade die Klasse 7/4 gewählt.
     * Damit wird durch diese Methode in der Klassenwahlliste die 7 markiert
     * und in der Nummerliste die 4
     *
     */
    private void refreshListSelections() {
        mCourseNumberList.post(() -> {
            ((NumberAdapter) mCourseNumberList.getAdapter()).setNumber(Database.courseNumber);
        });
        mCourseDegreeList.post(() -> {
            ((NumberAdapter) mCourseDegreeList.getAdapter()).setNumber(Database.courseDegree);
        });
        mTvNumber.setText(Database.courseNumber);
        mTvDegree.setText(Database.courseDegree);
    }

    private void blockButtons(long delay) {
        mButtonsBlocked = true;
        mHandler.postDelayed(() -> mButtonsBlocked = false, delay);
    }
}
