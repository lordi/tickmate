package de.smasi.tickmate.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.util.Calendar;

import de.smasi.tickmate.R;
import de.smasi.tickmate.Tickmate;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;

public class TickmateNotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "Tickmate";

    private static final String CHANNEL_ID = "Tickmate";

    @Override
    public void onReceive(Context context, Intent intent) {

        Boolean enabled = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("notification-enabled", false);

        Log.d(TAG, "Alarm received; enabled=" + enabled.toString());

        if (enabled) {


            createNotificationChannel(context);
            //Locale locale = Locale.getDefault();
            //Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale)

            NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.glyphicons_152_check_white)
                    .setContentTitle(context.getString(R.string.reminder_title))
                    .setAutoCancel(true)
                    .setContentText(context.getString(R.string.reminder_text));
            Intent resultIntent = new Intent(context, Tickmate.class);


            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
            NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(0, mBuilder.build());
        }
    }


    public static void updateAlarm(Context context) {
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

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

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

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.reminder_settings_title);
            String description = context.getString(R.string.reminder_settings_summary);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);
        }
    }
}
