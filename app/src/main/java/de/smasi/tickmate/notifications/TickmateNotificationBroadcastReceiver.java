package de.smasi.tickmate.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.preference.Preference;
import android.content.Context;
import android.content.Intent;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import de.smasi.tickmate.R;
import de.smasi.tickmate.Tickmate;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;

public class TickmateNotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "Tickmate";

    @Override
    public void onReceive(Context context, Intent intent) {

        Boolean enabled = PreferenceManager.getDefaultSharedPreferences(context)
                                           .getBoolean("notification-enabled", false);

        Log.d(TAG, "Alarm received; enabled=" + enabled.toString());

        if (enabled) {

            Locale locale = Locale.getDefault();

            java.text.DateFormat dateFormat = android.text.format.DateFormat
                    .getDateFormat(context);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.glyphicons_054_clock_white)
                            .setContentTitle("Tick reminder")
                            .setAutoCancel(true)
                            .setContentText("Tap to enter your ticks for " +
                                    Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale)
                            );
            Intent resultIntent = new Intent(context, Tickmate.class);


            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    public static void updateAlarm(Context context)
    {
        Calendar cal = Calendar.getInstance();
        java.text.DateFormat timeFmt = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);

        Boolean enabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("notification-enabled", false);

        String timeString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("notification-time", "");

        try {
            cal.setTime(timeFmt.parse(timeString));
        } catch (ParseException e) {
            Log.w(TAG, "Error parsing time: " + timeString);
            enabled = false;
        }

        Log.d(TAG, "Updating alarm; enabled=" + enabled.toString() + ", time=" + timeString);

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TickmateNotificationBroadcastReceiver.class);
        intent.putExtra("onetime", Boolean.FALSE);
        intent.addFlags(FLAG_INCLUDE_STOPPED_PACKAGES);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        // cancel any previous alarms
        am.cancel(pi);

        if (enabled) {

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            alarmTime.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            alarmTime.set(Calendar.SECOND, 0);
            alarmTime.set(Calendar.MILLISECOND, 0);

            if (alarmTime.before(Calendar.getInstance())) {
                alarmTime.add(Calendar.DAY_OF_YEAR, 1);
            }

            java.text.DateFormat dateFmt = java.text.DateFormat.getDateTimeInstance(
                    java.text.DateFormat.MEDIUM, java.text.DateFormat.LONG);

            Log.i(TAG, "Setting alarm at: " + dateFmt.format(alarmTime.getTime()));

            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(),
                                   AlarmManager.INTERVAL_DAY, pi);
        }

    }
}
