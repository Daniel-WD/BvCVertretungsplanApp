package com.titaniel.bvcvertretungsplan.authentication;

import android.os.AsyncTask;

import com.titaniel.bvcvertretungsplan.database.Database;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

/**
 * @author Daniel Weidensdörfer
 *
 * Klasse für die Überprüfung der Login Daten
 *
 * Man beachte, dass diese Klasse von AsyncTask ableitet, was eine Klasse aus dem Android SDK ist.
 * Sie ist dafür da, relativ kurze Operationen in einem separaten Thread zu erledigen und ein
 * Ergebnis an den Hauptthread zurückzuliefern.
 */
public class AuthTask extends AsyncTask<AuthTask.AuthData, Void, AuthTask.ResultData> {

    private static final String CHECK = "check.txt";

    /**
     * Klasse in der die Eingabedaten zusammengefasst werden
     */
    static class AuthData {
        AuthData(String user, String password, AuthManager.Callback callback) {
            this.user = user;
            this.password = password;
            this.callback = callback;
        }

        String user, password;
        AuthManager.Callback callback;
    }

    /**
     * Klasse für die Ausgabedaten
     */
    static class ResultData {
        ResultData(boolean success, AuthManager.Callback callback) {
            this.success = success;
            this.callback = callback;
        }

        boolean success;
        AuthManager.Callback callback;
    }

    /**
     * Überprüfung der Login Daten.
     * Wird von AsyncTask aufgerufen und in einem separaten Thread ausgeführt.
     *
     * @param input Inputdaten siehe <code>AuthData</code>
     * @return Ergebnis siehe <code>ResultData</code>
     */
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
        return new ResultData(success, input[0].callback);
    }

    /**
     * Übergabe des Ergebnisses an das Callback.
     * Wird von AsyncTask aufgerufen und im Hauptthread ausgeführt nachdem <code>doInBackground</code>
     * ausgeführt wurde
     *
     * @param output Ergebnis von <code>doInBackground</code>
     */
    @Override
    protected void onPostExecute(AuthTask.ResultData output) {
        if(output == null) return;
        output.callback.result(output.success);
    }
}
