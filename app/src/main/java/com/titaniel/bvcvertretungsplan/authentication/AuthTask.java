package com.titaniel.bvcvertretungsplan.authentication;

import android.os.AsyncTask;

import com.titaniel.bvcvertretungsplan.database.Database;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

public class AuthTask extends AsyncTask<AuthTask.AuthData, Void, AuthTask.ResultData> {

    private static final String CHECK = "check.txt";

    static class AuthData {
        AuthData(String user, String password, AuthManager.Callback callback) {
            this.user = user;
            this.password = password;
            this.callback = callback;
        }

        String user, password;
        AuthManager.Callback callback;
    }

    static class ResultData {

        ResultData(boolean success, AuthManager.Callback callback) {
            this.success = success;
            this.callback = callback;
        }

        boolean success;
        AuthManager.Callback callback;
    }

    @Override
    protected void onPostExecute(AuthTask.ResultData output) {
        if(output == null) return;
        output.callback.result(output.success);
    }

    @Override
    protected AuthTask.ResultData doInBackground(AuthTask.AuthData... input) {
        if(input[0] == null) return null;
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(input[0].user, input[0].password.toCharArray());
            }
        });

        boolean success = false;
        try {
            URL url = new URL(Database.SERVER_LOCATION + CHECK);
            String t = url.getFile();
            url.openStream().close();
            success = true;
        } catch (Exception ignored) {
        }

//        for(String name : DateManager.serverFileList) {
//            try {
//                URL url = new URL(Database.SERVER_LOCATION + name);
//                String t = url.getFile();
//                url.openStream().close();
//                success = true;
//                break;
//            } catch (Exception ignored) {
//            }
//        }
        return new ResultData(success, input[0].callback);
    }
}
