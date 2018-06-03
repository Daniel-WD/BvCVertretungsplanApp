package com.titaniel.bvcvertretungsplan.database;

import android.provider.ContactsContract;

import org.joda.time.LocalDateTime;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Daniel Weidensdörfer
 * Enthält Helferfunktionen für das Lesen der Daten aus den VP-Dateien
 */
public class DataUtils {

    /**
     * Liest die Monatsnummer aus dem Namen der VP-Datei aus
     * @param name Name der Datei
     * @return Monat
     */
    static int monthInName(String name) {
        return Integer.valueOf(name.substring(3, 5));
    }

    /**
     * Liest den Tag des Monats aus dem Namen der VP-Datei aus
     * @param name Name
     * @return Tag
     */
    static int dayInName(String name) {
        return Integer.valueOf(name.substring(5, 7));
    }

    /**
     * Liest das Jahr aud dem Namen der VP-Datei aus
     * @param name Name
     * @return Jahr
     */
    static int yearInName(String name) {
        return Integer.valueOf("20" + name.substring(1, 3));
    }

    /**
     * Liest den Tag des Monats aus dem zuletzt-aktualisiert-String der VP-Datei aus
     * @param date ausgelesener zuletzt-aktuallsiert-String
     * @return Tag
     */
    static int dayInDate(String date) {
        return Integer.valueOf(date.substring(0, 2));
    }

    /**
     * Liest den Monat aus dem zuletzt-aktualisiert-String der VP-Datei aus
     * @param date ausgelesener zuletzt-aktuallsiert-String
     * @return Monat
     */
    static int monthInDate(String date) {
        return Integer.valueOf(date.substring(3, 5));
    }

    /**
     * Liest das Jahr aus dem zuletzt-aktualisiert-String der VP-Datei aus
     * @param date ausgelesener zuletzt-aktuallsiert-String
     * @return Jahr
     */
    static int yearInDate(String date) {
        return Integer.valueOf(date.substring(6, 10));
    }

    /**
     * Liest die Stunden(Uhrzeit) aus dem zuletzt-aktualisiert-String der VP-Datei aus
     * @param date ausgelesener zuletzt-aktuallsiert-String
     * @return Stunden
     */
    static int hoursInDate(String date) {
        return Integer.valueOf(date.substring(12, 14));
    }

    /**
     * Liest die Minuten(Uhrzeit) aus dem zuletzt-aktualisiert-String der VP-Datei aus
     * @param date ausgelesener zuletzt-aktuallsiert-String
     * @return Minuten
     */
    static int minutesInDate(String date) {
        return Integer.valueOf(date.substring(15, 17));
    }

    /**
     * Sehr komplexe Methode.... :D ...deswegen eine umfangreichere Doku
     *
     * Gibt alle Klassen (siehe <code>Course</code> Objekt in <code>Database</code>) aus, die in
     * einem Kurs/Klassen String angesprochen werden.
     *
     * @param courseString Kurs/Klassen String
     * @return Im <code>courseString</code> enthaltene Kurse
     *
     * Beispiel:
     * Der übergebene String sei "10.1,10.2,10.4/ SpJu"
     *
     * Die Ausgabe ist ein Array mit allen Klassen:
     * Course::degree = 10, number = 1, specification = "SpJu"
     * Course::degree = 10, number = 2, specification = "SpJu"
     * Course::degree = 10, number = 4, specification = "SpJu"
     *
     * Dasselbe Funktioniert auch mit folgenden Fallbeispielen: 12, 10.1-10.3/Pnwi, 11/MA1
     */
    static Database.Course[] findCourses(String courseString) {
        ArrayList<Database.Course> result = new ArrayList<>(); //Ergebnis als Liste (wird in Schleifen aufgefüllt)
        ArrayList<String> blocks = new ArrayList<>(); //Blöcke... aus dem Beispiel: {10.1, 10.2, 10,4}
        String specification = ""; //Beispiel SpJu
        StringBuilder builder = new StringBuilder(courseString); //Für bessere Verarbeiung des <code>CourseString</code>

        //Finden der Blöcke
        if(builder.toString().contains(",")) { //wenn ein Komma enthalten ist -> mehrere Klassen
            while(builder.toString().contains(",")) { //solange ein Komma enthalten ist
                int index = builder.indexOf(","); //Position des Kommas
                blocks.add(builder.substring(0, index)); //Extrahieren eines Blocks (Bsp: 10.1) bis zum Komma(exklusiv)
                builder.replace(0, index+1, ""); //Entfernen des extrahierten Blocks bis zum Komma(inklusiv)
                if(builder.toString().contains(",")) { //Wenn noch ein Komma enthalten ist
                    continue; //nächster Schleifendurchlauf
                } else if(builder.toString().contains("/")) { //ansonsten, wenn ein Slash enthalten ist
                    int dex = builder.indexOf("/"); //Index des Slash
                    blocks.add(builder.substring(0, dex)); //letzten Block extrahieren (der bis zum Slash geht, da noch eine <code>specification</code>, wie "SpJu" vorhanden ist)
                    break;
                } else { //ansonsten (also kein Slash und Komma)
                    blocks.add(builder.substring(0, builder.length())); //letzten Block extrahieren (der bis zum String ende geht)
                    break;
                }
            }
        } else { //wenn von anfang an kein Komma enthalten ist
            //@me index to end or to '/'
            int dex = builder.indexOf("/"); //Index des Slash... wichtig: gibt -1 zurück, wenn kein Slash vorhanden ist
            dex = dex == -1 ? builder.length() : dex; //wenn kein Slash gefunden wurde, dann wird der Index zur Länge des Strings gesetzt
            blocks.add(builder.substring(0, dex)); //Extrahieren des einzelnen Blocks bis zum errechneten <code>dex</code>
        }

        //Suche der <code>specification</code>
        if(courseString.contains("/")) { //wenn Slash da ist (es gibt also etwas wie Pnwi oder MA1 oder SpJu)
            specification = courseString.substring(courseString.indexOf("/")+1).trim();
        }

        //umandeln der Blöcke in <code>Course</code>s
        for(String block : blocks) {
            if(block.contains("-")) { //Wenn ein Bindestrich im Block ist
                int index = block.indexOf("-");
                Database.Course startCourse = resolveBlock(block.substring(0, index), specification);
                Database.Course endCourse = resolveBlock(block.substring(index+1), specification);
                result.add(startCourse);
                result.add(endCourse);
                if(startCourse.degree == endCourse.degree && startCourse.number != 0 && endCourse.number != 0) {
                    for(int i = startCourse.number+1; i < endCourse.number; i++) { //durchgehen der Zwischenklassen
                        Database.Course newCourse = startCourse.copy();
                        newCourse.number = i;
                        result.add(newCourse); //<code>Course</code> zum ergebnis hinzufügen
                    }
                }
            } else { //kein Bindestrich
                Database.Course course = resolveBlock(block, specification);
                result.add(course);
            }
        }

        Database.Course[] res = new Database.Course[result.size()];
        return result.toArray(res);
    }

    /**
     * Macht aus einem Block und dessen <code>Specification</code> eine <code>Course</code> Klasse
     * @param block Blockstring
     * @param spec Specifcation
     * @return Course
     */
    private static Database.Course resolveBlock(String block, String spec) {
        Database.Course course = new Database.Course();
        course.degree = Integer.parseInt(block.substring(0, 2));

        if(course.degree != 11 && course.degree != 12) {
            course.number = Integer.parseInt(block.substring(3, 4));
        }
        course.specification = spec;
        return course;
    }

    /**
     * Wandelt einen Stunden String (1-2, 5-6, 7) in ein <code>Hours</code> Objekt um
     * @param hoursString Hoursstring
     * @return Hours-Objekt
     */
    static Database.Hours findHours(String hoursString) {
        Database.Hours res = new Database.Hours();
        for(int i = 0; i < hoursString.length(); i++) {
            char c = hoursString.charAt(i);
            if(Character.isDigit(c)) {
                if(res.startHour == 0) res.startHour = Integer.parseInt(String.valueOf(c));
                else if(res.endHour == 0) {
                    res.endHour = Integer.parseInt(String.valueOf(c));
                    break;
                }
            }
        }
        if(res.endHour == 0) res.endHour = res.startHour;
        return res;
    }

    /**
     * Ersetzt ein Komma, gefolgt von einem Leerzeichen, durch ein Komma, gefolgt von einer neuen Zeile
     * @param in Input String
     * @return Output String
     */
    static String wrapByComma(String in) {
        return in.replace(", ", ",\n");
    }

}
