package com.titaniel.bvcvertretungsplan;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.FileOutputStream;
import java.io.IOException;

public class QueryTask extends AsyncTask<Context, Void, Void> {

    private static final String TAG = QueryTask.class.getSimpleName();

    @Override
    protected Void doInBackground(Context... contexts) {
        FTPClient ftpClient = new FTPClient();

        try(FileOutputStream fos = contexts[0].openFileOutput("outputfile.xml", Context.MODE_PRIVATE)) {
            ftpClient.connect("w00a1664.kasserver.com");
            boolean login = ftpClient.login("f00c8ff8", "7C3wC69ThhQhGpuB");
            if (!login) {
                Log.d(TAG, "Connection fail...");
                return null;
            }
            Log.d(TAG, "connected");

            FTPFile[] files = ftpClient.listFiles();

            for (FTPFile file : files) {
                if (file.getType() == FTPFile.FILE_TYPE) {
                    Log.d(TAG, "File Name: " + file.getName());
                }
            }
            Log.d(TAG, contexts[0].getFilesDir().toString());
            boolean download = ftpClient.retrieveFile("k171121.xml", fos);
            if (download) {
                System.out.println("File downloaded successfully !");
            } else {
                System.out.println("Error in downloading file !");
            }

            // logout the user, returned true if logout successfully
            boolean logout = ftpClient.logout();
            if (logout) {
                Log.d(TAG, "Connection close...");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
