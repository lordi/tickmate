package de.smasi.tickmatedata.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Created by hannes on 04.09.15.
 */
public class ButtonHelpers {
    // Evaluates whether the tick (check) status for a given date should be changeable.
    // This depends on the preferences (have ticks been disable outside of today?)
    // the current date, and the date of this TickButton
    public static boolean isCheckChangePermitted(Context context, Calendar date) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String limitActivePref = sharedPrefs.getString("active-date-key", "ALLOW_ALL");

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        switch (limitActivePref) {
            case "ALLOW_CURRENT":
                return (date.compareTo(today) == 0);
            case "ALLOW_CURRENT_AND_NEXT_DAY":
                Calendar yesterday = (Calendar) today.clone();
                yesterday.add(Calendar.DATE, -1);
                return (date.compareTo(yesterday) >= 0);
            case "ALLOW_ALL":
            default:
                return true;
        }
    }
}
