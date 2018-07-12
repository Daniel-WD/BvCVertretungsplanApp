package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;
import android.view.View;

import com.titaniel.bvcvertretungsplan.date_manager.DateManager;
import com.titaniel.bvcvertretungsplan.R;
import com.titaniel.bvcvertretungsplan.main_activity.MainActivity;
import com.titaniel.bvcvertretungsplan.utils.Utils;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static com.titaniel.bvcvertretungsplan.connection_result.ConnectionResult.*;
import static com.titaniel.bvcvertretungsplan.database.Database.*;

/**
 * @author Daniel Weidensdörfer
 *
 * Klasse für das Downloaden der Daten und Verarbeiten dieser Daten
 *
 * Man beachte, dass diese Klasse von AsyncTask ableitet, was eine Klasse aus dem Android SDK ist.
 * Sie ist dafür da, relativ kurze Operationen in einem separaten Thread zu erledigen und ein
 * Ergebnis an den Hauptthread zurückzuliefern.
 *
 */
public class LoadingTask extends AsyncTask<LoadingTask.Input, Void, LoadingTask.LoadingResult> {

    private static final String TAG = LoadingTask.class.getSimpleName();

    /**
     * Repräsentiert eine URL mit dem jeweiligen Dateinamen
     */
    static class UrlHolder {
        URL url;
        String name;

        UrlHolder(URL url, String name) {
            this.url = url;
            this.name = name;
        }
    }

    /**
     * Ergebnis dieses Tasks
     */
    static class LoadingResult {
        Context context;
        int resultCode;

        LoadingResult(Context context, int resultcode) {
            this.context = context;
            this.resultCode = resultcode;
        }
    }

    /**
     * Eingabe für den Task
     */
    static class Input {
        Context context;
        boolean offline; //ob man offline ist, wenn ja dann nur Daten lesen und keinen Fehler werfen wenn keine Internetverbind da ist

        Input(Context context, boolean offline) {
            this.context = context;
            this.offline = offline;
        }
    }

    /**
     * Download und Lesen der Daten.
     * Wird von AsyncTask aufgerufen und in einem separaten Thread ausgeführt.
     *
     * @param inputs Input Daten
     * @return Ergebnis über Erfolg oder Fehlertyp
     */
    @Override
    protected LoadingResult doInBackground(LoadingTask.Input... inputs) {
        Context context = inputs[0].context;
        try {

            if(inputs[0].offline) { //OFFLINE
                readData(context, context.fileList());
                prepareEntries(context);

                //Day Manager
                DateManager.prepare();

                return new LoadingResult(context, RES_SUCCESS);
            }

            //ONLINE

            deleteAllFiles(context);

            //Alle Dateien auflisten, die erreichbar sind
            ArrayList<UrlHolder> allUrls = new ArrayList<>();
            for(String name : DateManager.serverFileList) {
                try {
                    URL url = new URL(Database.SERVER_LOCATION + name);
                    url.getFile();
                    url.openStream().close();
                    allUrls.add(new UrlHolder(url, name));
                } catch (Exception e) {
                    if(!Utils.isOnline(context)) return new LoadingResult(context, RES_NO_INTERNET);
                }
            }

            //Erreichbare Dateien Downloaden
            for(UrlHolder newFile : allUrls) {
                boolean success = downloadFile(context, newFile);
                if(!success) return new LoadingResult(context, RES_NO_INTERNET);
            }

            //read all files //Daten lesen
            readData(context, context.fileList());

            //prepare all entries for classification // Daten vorbereiten
            prepareEntries(context);

            //Day Manager // Datumssachen vorbereiten
            DateManager.prepare();

        } catch (IOException e) {
            e.printStackTrace();
            return new LoadingResult(context, RES_IO_EXCEPTION); //Lesefehler/Netzwerkfehler
        } catch (Exception e) {
            e.printStackTrace();
            return new LoadingResult(context, RES_XML_EXCEPTION); //anderer Fehler
        }
        return new LoadingResult(context, RES_SUCCESS);//kein Fehler
    }

    /**
     * Der <code>Activity</code> sagen, dass das Laden fertig ist/bzw fehlgeschlagen ist
     * Wird von AsyncTask aufgerufen und im Hauptthread ausgeführt nachdem <code>doInBackground</code>
     * ausgeführt wurde
     *
     * @param result Ergebnis von <code>doInBackground</code>
     */
    @Override
    protected void onPostExecute(LoadingResult result) {
        ((MainActivity) result.context).onLoaded(result.resultCode);
    }

    /**
     * Alle gespeicherten Daten löschen
     * @param context
     */
    private void deleteAllFiles(Context context) {
        for(String name : context.fileList()) {
            if(name.charAt(0) == 'k') {
                context.deleteFile(name);
            }
        }
    }

    /**
     * Eine Datei downloaden
     *
     * @param context Context
     * @param urlHolder UrlHolder
     * @return
     */
    private boolean downloadFile(Context context, UrlHolder urlHolder) {
        try {
            File file = new File(context.getFilesDir(), urlHolder.name);
            FileUtils.copyURLToFile(urlHolder.url, file); // todo new old techno
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Lesen aller daten
     * @param context Context
     * @param filenames Dateinamen
     * @throws IOException Lesefehler
     * @throws XmlPullParserException XMLFehler
     */
    private void readData(Context context, String[] filenames) throws IOException, XmlPullParserException {
        if(filenames == null) return;
        for(String name : filenames) {
            readFile(context, name);
        }
    }

    /**
     * Lesen einer XML Datei
     *
     * @param context Context
     * @param _name Name
     * @throws IOException Lesefehler
     * @throws XmlPullParserException XMLFehler
     */
    private void readFile(Context context, String _name) throws IOException, XmlPullParserException {
        if(!Arrays.asList(DateManager.serverFileList).contains(_name)) return;
        Database.Day day = new Database.Day();
        day.name = _name;
        Database.Entry entry = null;

        day.date = new LocalDate(DataUtils.yearInName(_name),
                DataUtils.monthInName(_name),
                DataUtils.dayInName(_name));

/*        try(InputStream is = context.openFileInput(_name)) { //für mich
            Scanner s = new Scanner(is);

            while(s.hasNextLine()) {
                Log.d("hallo", s.nextLine());
            }
        }*/

        try(InputStream is = context.openFileInput(_name)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.next();

            parser.require(XmlPullParser.START_TAG, null, "vp");
            while(parser.next() != XmlPullParser.END_DOCUMENT) { //geht alle Zeilen durch und guckt was da drin steht :D, dementsprechend werden bestimmte Operationen durchgeführt
                switch(parser.getEventType()) {
                    case XmlPullParser.START_TAG:
                        switch(parser.getName()) {
                            case KEY_LAST_UPDATED:
                                parser.next();
                                String lastUpdateText = parser.getText();
                                day.lastUpdate = new LocalDateTime(
                                        DataUtils.yearInDate(lastUpdateText),
                                        DataUtils.monthInDate(lastUpdateText),
                                        DataUtils.dayInDate(lastUpdateText),
                                        DataUtils.hoursInDate(lastUpdateText),
                                        DataUtils.minutesInDate(lastUpdateText));
                                continue;

                            case KEY_DISABLED_COURSES:
                                parser.next();
                                day.disabledClasses = parser.getText();
                                continue;

                            case KEY_DISABLED_ROOMS:
                                parser.next();
                                day.disabledRooms = parser.getText();
                                continue;

                            case KEY_ENTRY:
                                entry = new Database.Entry();
                                continue;

                            case KEY_COURSE:
                                parser.next();
                                if(entry != null) entry.courseString = parser.getText();
                                continue;

                            case KEY_HOURS:
                                parser.next();
                                if(entry != null) entry.hoursString = parser.getText();
                                continue;

                            case KEY_LESSON:
                                // TODO: 11.02.2018 name
                                if(parser.getAttributeCount() > 0 &&
                                        parser.getAttributeValue(0).equals(KEY_TRUE)) {
                                    if(entry != null) entry.lessonChange = true;
                                }
                                parser.next();
                                if(entry != null) entry.lesson = parser.getText();
                                continue;

                            case KEY_TEACHER:
                                // TODO: 11.02.2018 name
                                if(parser.getAttributeCount() > 0 &&
                                        parser.getAttributeValue(0).equals(KEY_TRUE)) {
                                    if(entry != null) entry.teacherChange = true;
                                }
                                parser.next();
                                if(entry != null) entry.teacher = parser.getText();
                                continue;

                            case KEY_ROOM:
                                // TODO: 11.02.2018 name
                                if(parser.getAttributeCount() > 0 &&
                                        parser.getAttributeValue(0).equals(KEY_TRUE)) {
                                    if(entry != null) entry.roomChange = true;
                                }
                                parser.next();
                                if(entry != null) entry.room = parser.getText();
                                continue;

                            case KEY_INFO:
                                parser.next();
                                if(entry != null) entry.info = parser.getText();
                                continue;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        switch(parser.getName()) {
                            case KEY_ENTRY:
                                day.entries.add(entry);
                                entry = null;
                                continue;
                        }
                        break;
                }
            }
            Database.days.add(day);
        }
    }

    /**
     * Essentiel um das Filtern zu ermöglichen
     * Geht alle Einträge in jedem Tag durch und bestimmt <code>Hours</code> und <code>Course</code> Objekte
     *
     * @param context Context
     */
    private void prepareEntries(Context context) {
        for(Database.Day day : Database.days) {
            ArrayList<Database.Entry> newEntries = new ArrayList<>();
            for(Database.Entry entry : day.entries) {
                //hours processing
                entry.hours = DataUtils.findHours(entry.hoursString);

                //course processing
                Database.Course[] courses = DataUtils.findCourses(entry.courseString);
                entry.course = courses[0];

                if(entry.hours.startHour == entry.hours.endHour) {
                    entry.hoursText = String.valueOf(entry.hours.startHour);
                } else {
                    entry.hoursText = context.getString(R.string.temp_hours, entry.hours.startHour, entry.hours.endHour);
                }

                entry.specVisible = entry.course.specification.equals("") ? View.GONE : View.VISIBLE; //für angezeigte Liste

                entry.lesson = entry.lesson == null ? "---" : entry.lesson;
                entry.teacher = entry.teacher == null ? "---" : entry.teacher;
                entry.room = entry.room == null ? "---" : entry.room;
                entry.info = entry.info == null ? "keine Info" : entry.info;

                entry.teacher = DataUtils.wrapByComma(entry.teacher);
                entry.room = DataUtils.wrapByComma(entry.room);

//                entry.lessonChangeVisible = entry.lessonChange && !entry.lesson.equals("---") ? View.VISIBLE : View.INVISIBLE;
//                entry.teacherChangeVisible = entry.teacherChange && !entry.teacher.equals("---") ? View.VISIBLE : View.INVISIBLE;
//                entry.roomChangeVisible = entry.roomChange && !entry.room.equals("---") ? View.VISIBLE : View.INVISIBLE;

                /*if(entry.room.equals("---") && entry.lesson.equals("---") && entry.teacher.equals("---")) {
                    entry.lesson = "Ausfall";
                    entry.room = "";
                    entry.teacher = "";
                }*/

                //Vervielfältigund eines Eintrags wenn er für mehrere Klassen bestimmt ist
                //Beispiel:
                // Ein Eintrag mit der Angabe: "10.1,10.2,10.4/ SpJu"
                // wird in 3 Einträge mit den Angaben "10.1/ SpJu", "10.2/ SpJu", "10.4/ SpJu" umgewandelt
                for(int i = 1; i < courses.length; i++) {
                    Database.Entry newEntry = entry.copy();
                    newEntry.course = courses[i];
                    newEntries.add(newEntry);
                }

            }
            day.entries.addAll(newEntries);
        }
    }
}
