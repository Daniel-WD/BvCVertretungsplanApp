package com.titaniel.bvcvertretungsplan.authentication;

import android.content.Context;

import com.titaniel.bvcvertretungsplan.database.Database;

/**
 * @author Daniel Weidensdörfer
 *
 * Helfer Klasse zum überprüfen, ob bestimmte Login Daten stimmen.
 *
 * Diese Klasse ist nicht unbedingt nötig, macht den aufrufenden Code jedoch übersichtlicher.
 *
 */
public class AuthManager {

    /**
     * Interface für den Rückgabewert
     */
    @FunctionalInterface
    public interface Callback {
        /**
         * Rückgabemethode für den Erfolg der Login Daten Überprüfung
         * @param success true wenn Login Daten stimmen, ansonsten false
         */
        void result(boolean success);
    }

    /**
     * Aufruf des Threads, der die Logindaten prüft
     * @param user Nutzername
     * @param password Passwort
     * @param callback Callback(ob Erfolg oder kein Erfolg)
     */
    public static void checkLogin(Context context, String user, String password, Callback callback) {
        new AuthTask().execute(new AuthTask.AuthData(context, user, password, callback));
    }

    /**
     * Nutzt in der <code>Database</code> gespeicherten Nutzernamen und Passwort
     * @see AuthManager#checkLogin(Context, String, String, Callback)
     * @param callback Callback
     */
    public static void checkLogin(Context context, Callback callback) {
        checkLogin(context, Database.username, Database.password, callback);
    }

}
