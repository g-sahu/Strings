package com.mediaplayer.strings.utilities;

import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;

import static com.mediaplayer.strings.utilities.SQLConstants.ZERO;

public class Utilities {
    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
    public static String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String minutesString, secondsString;

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000*60*60));
        int minutes = (int) (milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);

        // Add hours if there
        finalTimerString = (hours > 0) ? hours + ":" : finalTimerString;

        // Prepending 0 to minutes if it is one digit
        minutesString = (minutes < 10) ? "0" + minutes : "" + minutes;

        // Prepending 0 to seconds if it is one digit
        secondsString = (seconds < 10) ? "0" + seconds : "" + seconds;

        finalTimerString = finalTimerString + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration Elapsed duration of the track
     * @param totalDuration Total track duration
     * */
    public static int getProgressPercentage(long currentDuration, long totalDuration){
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        Double percentage = (((double) currentSeconds)/totalSeconds) * 100;
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);
        return currentDuration * 1000;
    }

    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(SQLConstants.DD_MM_YYYY);
        return df.format(c.getTime());
    }

    /*public static void reportCrash(Exception e) {
        FirebaseCrash.log(e.getMessage());
        FirebaseCrash.logcat(Log.ERROR, MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
        FirebaseCrash.report(e);
    }*/

    public static boolean isNotNullOrEmpty(Collection collection) {
        return (collection != null && !collection.isEmpty());
    }

    public static boolean isNotNullOrEmpty(Cursor cursor) {
        return (cursor != null && cursor.getCount() > ZERO);
    }
}
