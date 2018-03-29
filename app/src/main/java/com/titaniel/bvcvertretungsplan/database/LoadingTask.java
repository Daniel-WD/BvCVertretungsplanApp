package com.titaniel.bvcvertretungsplan.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import com.titaniel.bvcvertretungsplan.MainActivity;
import com.titaniel.bvcvertretungsplan.R;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
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

    static class LoadingResult {
        Context context;
        boolean ioException;
        boolean otherException;

        public LoadingResult(Context context, boolean ioException, boolean otherException) {
            this.context = context;
            this.ioException = ioException;
            this.otherException = otherException;
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
        ((MainActivity) result.context).onDatabaseLoaded(result.ioException, result.otherException);
    }

    @Override
    protected LoadingResult doInBackground(LoadingTask.Input... inputs) {
        Context context = inputs[0].context;
        FTPClient ftpClient = new FTPClient();
        try {
            // TODO: 15.02.2018 delete all outdated files

/*            try {

                ReadableByteChannel in= Channels.newChannel(
                        new URL("ftp://www.cottagym.selfhost.eu/var/www/html/images/cottaintern/vp/k180326.xml").openStream());


                    out.transferFrom(in, 0, Long.MAX_VALUE);

                URL url = new URL("http://www.cottagym.selfhost.eu/var/www/htm/images/cottaintern/vp/k180326.xml");
                FileUtils.copyURLToFile(url, new File(""));

                InetAddress addr;
                Socket sock = new Socket("www.cottagym.selfhost.eu", 80);
                addr = sock.getInetAddress();
                System.out.println("Connected to " + addr);
                sock.close();
                System.out.println("Connected to " + addr);
            } catch (java.io.IOException e) {
                System.out.println("Can't connect");
                System.out.println(e);
            }*/

            //read all current existing files
            readData(context, context.fileList());
            if(inputs[0].offline) {
                prepareEntries(context);
                return new LoadingResult(context, false, false);
            }

            //connect
            ftpClient.connect("www.cottagym.selfhost.eu");

            //login
            boolean loggedIn = ftpClient.login("schueler", "vpcotta_18");
            if(!loggedIn) return new LoadingResult(context, true, false);

            //list files
            FTPFile[] allFiles = ftpClient.listFiles();

            //filter new files... checking if file already exist and if it is already up to date
            ArrayList<FTPFile> newFiles = new ArrayList<>();
            for(FTPFile file : allFiles) {
                if(file.getType() != FTPFile.FILE_TYPE || file.getName().charAt(0) != 'k') {
                    continue;
                }
                Database.Day day;
                if((day = DataUtils.findDay(file)) == null ||
                        !DataUtils.calendarToLocalDateTime(file.getTimestamp()).isEqual(day.lastUpdate)) {
                    Database.days.remove(day);
                    newFiles.add(file);
                }
            }


/*            for(FTPFile file : allFiles) {
                if(file.getType() == FTPFile.FILE_TYPE &&
                        !Database.savedFiles.contains(file.getName()) &&
                        file.getName().charAt(0) == 'k') {
                    newFiles.add(file);
                }
            }*/

            //download new files
            for(FTPFile newFile : newFiles) {
                downloadFile(context, ftpClient, newFile);
            }

            //read all updated and new files
            readData(context, DataUtils.toStringArray(newFiles));

            //prepare all entries for classification
            prepareEntries(context);

            for(Database.Day day : Database.days) {
                Log.d("da", "da");
                for(Database.Entry entry : day.entries) {
                    Log.d("hallo?", "str::" + entry.courseString + " --- " + entry.course.toString());
                }
            }

            //logout
            ftpClient.logout();
        } catch (IOException e) {
            e.printStackTrace();
            return new LoadingResult(context, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            return new LoadingResult(context, false, true);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new LoadingResult(context, false, false);
    }

    private boolean downloadFile(Context context, FTPClient client, FTPFile file) {
        context.deleteFile(file.getName());
        try(FileOutputStream fos = context.openFileOutput(file.getName(), Context.MODE_PRIVATE)) {
            return client.retrieveFile(file.getName(), fos);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void readData(Context context, String[] filenames) throws IOException, XmlPullParserException {
        if(filenames == null) return;
        for(String name : filenames) {
            readFile(context, name);
        }
    }

    private void readFile(Context context, String _name) throws IOException, XmlPullParserException {
        if(_name.charAt(0) != 'k') return;
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

                entry.lessonChangeVisible = entry.lessonChange && !entry.lesson.equals("---") ? View.VISIBLE : View.GONE;
                entry.teacherChangeVisible = entry.teacherChange && !entry.teacher.equals("---") ? View.VISIBLE : View.GONE;
                entry.roomChangeVisible = entry.roomChange && !entry.room.equals("---") ? View.VISIBLE : View.GONE;
                entry.breakOutVisible =
                        entry.room.equals("---") &&
                        entry.lesson.equals("---") &&
                        entry.teacher.equals("---") ? View.VISIBLE : View.GONE;

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
