package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import com.titaniel.bvcvertretungsplan.utils.DateManager;
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
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static com.titaniel.bvcvertretungsplan.database.Database.KEY_COURSE;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_DISABLED_COURSES;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_DISABLED_ROOMS;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_ENTRY;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_HOURS;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_INFO;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_LAST_UPDATED;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_LESSON;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_ROOM;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_TEACHER;
import static com.titaniel.bvcvertretungsplan.database.Database.KEY_TRUE;

public class LoadingTask extends AsyncTask<LoadingTask.Input, Void, LoadingTask.LoadingResult> {

    private static final String TAG = LoadingTask.class.getSimpleName();

    private static final int M_BYTE = 1048576;

    static class UrlHolder {
        URL url;
        String name;

        UrlHolder(URL url, String name) {
            this.url = url;
            this.name = name;
        }
    }

    static class LoadingResult {
        Context context;
        boolean ioException;
        boolean otherException;
        boolean internetCut;

        LoadingResult(Context context, boolean ioException, boolean otherException, boolean internetCut) {
            this.context = context;
            this.ioException = ioException;
            this.otherException = otherException;
            this.internetCut = internetCut;
        }
    }

    static class Input {
        Context context;
        boolean offline;

        Input(Context context, boolean offline) {
            this.context = context;
            this.offline = offline;
        }
    }

    @Override
    protected void onPostExecute(LoadingResult result) {
        ((MainActivity) result.context).onDatabaseLoaded(result.ioException, result.otherException, result.internetCut);
    }

    @Override
    protected LoadingResult doInBackground(LoadingTask.Input... inputs) {
        Context context = inputs[0].context;
        try {
            // TODO: 15.02.2018 delete all outdated files

            //offline
            //we are offline and we only load already downloaded data
            if(inputs[0].offline) {
                //read all current existing files
                readData(context, context.fileList());
                prepareEntries(context);

                //Day Manager
                DateManager.prepare();

                return new LoadingResult(context, false, false, false);
            }

            //online

            /*Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("schueler", "vpcotta_18".toCharArray());
                }
            });*/

            deleteAllFiles(context);

            //list files
            ArrayList<UrlHolder> allUrls = new ArrayList<>();
            for(String name : DateManager.serverFileList) {
                try {
                    URL url = new URL(Database.SERVER_LOCATION + name);
                    String t = url.getFile();
                    url.openStream().close();
                    allUrls.add(new UrlHolder(url, name));
                } catch (Exception e) {
                    if(!Utils.isOnline(context)) new LoadingResult(context, false, false, true);
                }
            }

            //download all files
            for(UrlHolder newFile : allUrls) {
                boolean success = downloadFile(context, newFile);
                if(!success) return new LoadingResult(context, false, false, true);
            }

            //read all files
            readData(context, context.fileList());

            //prepare all entries for classification
            prepareEntries(context);

            //Day Manager
            DateManager.prepare();

            /*for(Database.Day day : Database.days) {
                Log.d("da", "da");
                for(Database.Entry entry : day.entries) {
                    Log.d("hallo?", "str::" + entry.courseString + " --- " + entry.course.toString());
                }
            }*/

        } catch (IOException e) {
            e.printStackTrace();
            return new LoadingResult(context, true, false, false);
        } catch (Exception e) {
            e.printStackTrace();
            return new LoadingResult(context, false, true, false);
        }
        return new LoadingResult(context, false, false, false);
    }

    private void deleteAllFiles(Context context) {
        for(String name : context.fileList()) {
            if(name.charAt(0) == 'k') {
                context.deleteFile(name);
            }
        }
    }

    private boolean downloadFile(Context context, UrlHolder urlHolder) {
        //                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
//                String line = null;
//                while((line = br.readLine())!= null){
//                    System.out.println(line);
//                }
//                br.close();

        try/*(InputStream is = urlHolder.url.openStream();
                FileOutputStream fos = context.openFileOutput(urlHolder.name, Context.MODE_PRIVATE);
                ReadableByteChannel rbc = Channels.newChannel(urlHolder.url.openStream()))*/ {
            //fos.getChannel().transferFrom(rbc, 0, 10*M_BYTE);

            File file = new File(context.getFilesDir(), urlHolder.name);
            FileUtils.copyURLToFile(urlHolder.url, file); // todo new old techno
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void readData(Context context, String[] filenames) throws IOException, XmlPullParserException {
        if(filenames == null) return;
        for(String name : filenames) {
            readFile(context, name);
        }
    }

    private void readFile(Context context, String _name) throws IOException, XmlPullParserException {
        if(!Arrays.asList(DateManager.serverFileList).contains(_name)) return;
        Database.Day day = new Database.Day();
        day.name = _name;
        Database.Entry entry = null;

        day.date = new LocalDate(DataUtils.yearInName(_name),
                DataUtils.monthInName(_name),
                DataUtils.dayInName(_name));

        try(InputStream is = context.openFileInput(_name)) {
            Scanner s = new Scanner(is);

            while(s.hasNextLine()) {
                Log.d("hallo", s.nextLine());
            }
        }

        try(InputStream is = context.openFileInput(_name)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.next();

            parser.require(XmlPullParser.START_TAG, null, "vp");
            while(parser.next() != XmlPullParser.END_DOCUMENT) {
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
                                day.disabledCourses = parser.getText();
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
                                entry.courseString = parser.getText();
                                continue;

                            case KEY_HOURS:
                                parser.next();
                                entry.hoursString = parser.getText();
                                continue;

                            case KEY_LESSON:
                                // TODO: 11.02.2018 name
                                if(parser.getAttributeCount() > 0 &&
                                        parser.getAttributeValue(0).equals(KEY_TRUE)) {
                                    entry.lessonChange = true;
                                }
                                parser.next();
                                entry.lesson = parser.getText();
                                continue;

                            case KEY_TEACHER:
                                // TODO: 11.02.2018 name
                                if(parser.getAttributeCount() > 0 &&
                                        parser.getAttributeValue(0).equals(KEY_TRUE)) {
                                    entry.teacherChange = true;
                                }
                                parser.next();
                                entry.teacher = parser.getText();
                                continue;

                            case KEY_ROOM:
                                // TODO: 11.02.2018 name
                                if(parser.getAttributeCount() > 0 &&
                                        parser.getAttributeValue(0).equals(KEY_TRUE)) {
                                    entry.roomChange = true;
                                }
                                parser.next();
                                entry.room = parser.getText();
                                continue;

                            case KEY_INFO:
                                parser.next();
                                entry.info = parser.getText();
                                continue;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        switch(parser.getName()) {
                            case KEY_ENTRY:
                                day.entries.add(entry);
                                continue;
                        }
                        break;
                }
            }
            Log.d(TAG, day.date.toString());
            Log.d(TAG, day.lastUpdate.toString());
            Database.days.add(day);
        }
    }

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

                entry.specVisible = entry.course.specification.equals("") ? View.GONE : View.VISIBLE;

                entry.lesson = entry.lesson == null ? "---" : entry.lesson;
                entry.teacher = entry.teacher == null ? "---" : entry.teacher;
                entry.room = entry.room == null ? "---" : entry.room;
                entry.info = entry.info == null ? "keine Info" : entry.info;

                entry.teacher = DataUtils.wrapByComma(entry.teacher);
                entry.room = DataUtils.wrapByComma(entry.room);

                entry.lessonChangeVisible = entry.lessonChange && !entry.lesson.equals("---") ? View.VISIBLE : View.INVISIBLE;
                entry.teacherChangeVisible = entry.teacherChange && !entry.teacher.equals("---") ? View.VISIBLE : View.INVISIBLE;
                entry.roomChangeVisible = entry.roomChange && !entry.room.equals("---") ? View.VISIBLE : View.INVISIBLE;

                /*if(entry.room.equals("---") && entry.lesson.equals("---") && entry.teacher.equals("---")) {
                    entry.lesson = "Ausfall";
                    entry.room = "";
                    entry.teacher = "";
                }*/

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
