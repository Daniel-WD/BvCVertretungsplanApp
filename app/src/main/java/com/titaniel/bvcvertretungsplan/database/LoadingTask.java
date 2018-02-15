package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.titaniel.bvcvertretungsplan.MainActivity;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

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

public class LoadingTask extends AsyncTask<Context, Void, Context> {

    private static final String TAG = LoadingTask.class.getSimpleName();

    @Override
    protected void onPostExecute(Context context) {
        ((MainActivity) context).onDatabaseLoaded();
    }

    @Override
    protected Context doInBackground(Context... contexts) {
        FTPClient ftpClient = new FTPClient();

        try {
            //connect
            ftpClient.connect("w00a1664.kasserver.com");

            //login
            boolean loggedIn = ftpClient.login("f00c8ff8", "7C3wC69ThhQhGpuB");
            if(!loggedIn) return null;

            //list files
            FTPFile[] allFiles = ftpClient.listFiles();

            //get last update
            for(FTPFile file : allFiles) {
                if(file.getType() != FTPFile.FILE_TYPE ||
                        file.getName().charAt(0) != 'k') {
                    continue;
                }
                Calendar cal = file.getTimestamp();
                LocalDateTime dateTime = new LocalDateTime(cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH)+1,
                        cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        cal.get(Calendar.SECOND));
                Log.d(TAG, dateTime.toString());
            }

            //filter new files
            ArrayList<FTPFile> newFiles = new ArrayList<>();
            for(FTPFile file : allFiles) {
                if(file.getType() == FTPFile.FILE_TYPE &&
                        !Database.savedFiles.contains(file.getName()) &&
                        file.getName().charAt(0) == 'k') {
                    newFiles.add(file);
                }
            }

            //download new files
            for(FTPFile newFile : newFiles) {
                downloadFile(contexts[0], ftpClient, newFile);
                Database.savedFiles.add(newFile.getName());
            }

            //logout
            ftpClient.logout();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        readData(contexts[0]);
        return contexts[0];
    }

    private boolean downloadFile(Context context, FTPClient client, FTPFile file) {
        try(FileOutputStream fos = context.openFileOutput(file.getName(), Context.MODE_PRIVATE)) {
            return client.retrieveFile(file.getName(), fos);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void readData(Context context) {
        for(String name : Database.savedFiles) {
            readFile(context, name);
        }
    }

    private void readFile(Context context, String _name) {
        if(_name.charAt(0) != 'k') return;
        Database.Day day = new Database.Day();
        Database.Entry entry = null;

        day.date = new LocalDate(DataUtils.yearInName(_name),
                DataUtils.monthInName(_name),
                DataUtils.dayInName(_name));

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
                                entry.course = parser.getText();
                                continue;

                            case KEY_HOURS:
                                parser.next();
                                entry.hours = parser.getText();
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
                        }
                        break;
                }
            }
            Log.d(TAG, day.date.toString());
            Log.d(TAG, day.lastUpdate.toString());
            Database.days.add(day);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
