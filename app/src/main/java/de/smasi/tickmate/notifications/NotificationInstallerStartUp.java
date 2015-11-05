package de.smasi.tickmate.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import de.smasi.tickmate.R;
import de.smasi.tickmate.Tickmate;

public class NotificationInstallerStartUp extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Tickmate", "NotificationInstallerStartUp.onReceive");
        TickmateNotificationBroadcastReceiver.activateAlarm(context);
    }

}
