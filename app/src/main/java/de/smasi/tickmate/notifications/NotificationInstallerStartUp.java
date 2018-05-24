package de.smasi.tickmate.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationInstallerStartUp extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Tickmate", "NotificationInstallerStartUp.onReceive");
        TickmateNotificationBroadcastReceiver.updateAlarm(context);
    }

}
