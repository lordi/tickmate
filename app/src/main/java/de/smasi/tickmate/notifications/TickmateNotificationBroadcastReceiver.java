package de.smasi.tickmate.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

import de.smasi.tickmate.R;
import de.smasi.tickmate.Tickmate;

public class TickmateNotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "Tickmate";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm received");
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

    public static void activateAlarm(Context context)
    {
        Log.i(TAG, "Setting alarm at 12 o clock...");

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TickmateNotificationBroadcastReceiver.class);
        intent.putExtra("onetime", Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        // cancel any previous alarms
        am.cancel(pi);

        //After after 30 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
    }
}
